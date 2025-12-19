package com.example.classpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.classpass.ui.compose.MainApp
import com.example.classpass.ui.theme.ClassPassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Disable edge-to-edge helper (we'll do it manually)
        // enableEdgeToEdge()
        
        // Enable edge-to-edge manually with full control
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make system bars completely transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        // Set system bar icons to dark color for dark background
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = false
        windowInsetsController?.isAppearanceLightNavigationBars = false
        
        try {
            setContent {
                ClassPassTheme(darkTheme = true) {
                    // No Surface wrapper - let MainApp handle its own background
                    MainApp()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainActivity", "Error setting content", e)
        }
    }
}