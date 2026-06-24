package com.example.smartsplit.ui.screens

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartsplit.util.ReceiptAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    onBackClick: () -> Unit = {},
    onAmountScanned: (Double) -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    var scannedTotal by remember { mutableStateOf<Double?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreview(
                    onTotalExtracted = { total ->
                        // Only update if we found a bigger number or if it's the first one
                        if (scannedTotal == null || total > scannedTotal!!) {
                            scannedTotal = total
                        }
                    }
                )
            } else {
                Text(
                    text = "Camera permission is required to scan receipts.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Overlay for scanned amount
            scannedTotal?.let { total ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Detected Total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$total LEI",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAmountScanned(total) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use this amount")
                        }
                    }
                }
            }
            
            // Viewfinder frame overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                 // You can draw a border here if you want a viewfinder UI
            }
        }
    }
}

@Composable
fun CameraPreview(
    onTotalExtracted: (Double) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            ReceiptAnalyzer(onTotalExtracted)
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    // Log error
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
