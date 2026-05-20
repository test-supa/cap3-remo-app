package com.ai.assistant.jarvis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class ScreenStateReceiver : BroadcastReceiver() {
    private val TAG = "JarvisScreenState"

    companion object {
        private const val SUPABASE_URL = "https://adicrtqpyccthnmacgzu.supabase.co/rest/v1"
        private const val SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFkaWNydHFweWNjdGhubWFjZ3p1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxODI4NTIsImV4cCI6MjA5Mjc1ODg1Mn0." +
            "KUn9Nf1WGiBb31mEHrU5-NDo_K-BEUA0laGL6yFdnT0"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "Screen went OFF")
                updateScreenState(context, "Sleeping")
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Screen went ON")
                updateScreenState(context, "Online")
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "Device unlocked")
                updateScreenState(context, "Active")
            }
        }
    }

    private fun updateScreenState(context: Context, status: String) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()

        scope.launch {
            val body = JSONObject().apply {
                put("status", status)
                put("last_seen", java.time.Instant.now().toString())
            }.toString()

            val request = Request.Builder()
                .url("$SUPABASE_URL/jarvis_devices?device_id=eq.$deviceId")
                .headers(supabaseHeaders())
                .patch(body.toRequestBody("application/json".toMediaType()))
                .build()

            runCatching { http.newCall(request).execute() }.onSuccess { resp ->
                Log.d(TAG, "Screen state → $status (HTTP ${resp.code})")
                resp.close()
            }.onFailure {
                Log.e(TAG, "Failed to update screen state: ${it.message}")
            }
        }
    }

    private fun supabaseHeaders(): Headers = Headers.Builder()
        .add("apikey", SUPABASE_ANON_KEY)
        .add("Authorization", "Bearer $SUPABASE_ANON_KEY")
        .add("Content-Type", "application/json")
        .build()
}
