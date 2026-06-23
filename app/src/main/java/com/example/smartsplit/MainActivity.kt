package com.example.smartsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartsplit.ui.navigation.AppNavigation
import com.example.smartsplit.ui.theme.SmartSplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSplitTheme {
                AppNavigation()
            }
        }
    }
}