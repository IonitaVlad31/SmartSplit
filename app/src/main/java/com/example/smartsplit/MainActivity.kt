package com.example.smartsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import com.example.smartsplit.ui.navigation.AppNavigation
import com.example.smartsplit.ui.theme.SmartSplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSplitTheme {
                androidx.compose.material3.Surface(
                    modifier = androidx.compose.ui.Modifier.background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color(0xFF150A0A), // Very subtle, almost black dark red
                                androidx.compose.ui.graphics.Color(0xFF0A0A0A)  // True dark background
                            ),
                            radius = 2000f
                        )
                    ),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    AppNavigation()
                }
            }
        }
    }
}