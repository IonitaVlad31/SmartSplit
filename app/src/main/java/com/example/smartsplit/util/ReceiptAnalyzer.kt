package com.example.smartsplit.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ReceiptAnalyzer(
    private val onTotalExtracted: (Double) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    
    private var lastAnalyzedTimestamp = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < 2000) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val total = extractHighestNumber(visionText.text)
                    if (total > 0) {
                        onTotalExtracted(total)
                        lastAnalyzedTimestamp = currentTimestamp
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptAnalyzer", "Text recognition failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    
    private fun extractHighestNumber(text: String): Double {
        val regex = Regex("""\d+[\.,]\d{2}""")
        val matches = regex.findAll(text)
        
        var maxAmount = 0.0
        for (match in matches) {
            
            val numStr = match.value.replace(',', '.')
            val num = numStr.toDoubleOrNull()
            if (num != null && num > maxAmount) {
                maxAmount = num
            }
        }
        return maxAmount
    }
}
