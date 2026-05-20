package com.ai.assistant.jarvis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver — auto-starts JarvisAgentService after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot detected — starting Jarvis C2 agent")
            JarvisAgentService.start(context)
        }
    }
}
