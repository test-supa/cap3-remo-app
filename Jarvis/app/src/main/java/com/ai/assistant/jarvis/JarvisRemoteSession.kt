package com.ai.assistant.jarvis

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hmdm.control.Const
import com.hmdm.control.SettingsHelper
import com.hmdm.control.SharingEngine
import com.hmdm.control.janus.SharingEngineJanus
import kotlinx.coroutines.*

class JarvisRemoteSession(
    private val context:      Context,
    private val serverHost:   String,
    private val serverSecret: String,
    private val sessionId:    String,
    private val onConnected:  suspend () -> Unit,
    private val onEnded:      suspend () -> Unit
) {

    companion object {
        private const val TAG = "JarvisRemoteSession"
    }

    private var sharingEngine: SharingEngineJanus? = null
    private var engineLaunched = false

    private val mainHandler = Handler(Looper.getMainLooper())

    fun start(offerSdp: String) {
        if (engineLaunched) return
        engineLaunched = true

        Log.d(TAG, "Starting remote session $sessionId → $serverHost")

        // Configure SharedPreferences so SharingEngineJanus can read server URL
        SettingsHelper.getInstance(context).apply {
            setString(SettingsHelper.KEY_SERVER_URL, "http://$serverHost:8088/")
            setString(SettingsHelper.KEY_SECRET, serverSecret)
            setString(SettingsHelper.KEY_DEVICE_NAME, "JarvisAI")
        }

        // SharingEngineJanus creates a Handler internally, so it must run on main thread
        Handler(Looper.getMainLooper()).post(Runnable {
            sharingEngine = SharingEngineJanus().apply {
                setEventListener(object : SharingEngine.EventListener {
                    override fun onStartSharing(username: String?) {
                        Log.d(TAG, "Sharing started for user: $username")
                    }
                    override fun onStopSharing() {
                        Log.d(TAG, "Sharing stopped")
                        stop()
                    }
                    override fun onPing() {
                        Log.d(TAG, "Ping from server")
                    }
                    override fun onRemoteControlEvent(event: String?) {
                        Log.d(TAG, "Remote control: $event")
                    }
                })
                setStateListener { state ->
                    Log.d(TAG, "Sharing state: $state")
                    when (state) {
                        Const.STATE_CONNECTED -> {
                            CoroutineScope(Dispatchers.IO).launch { onConnected() }
                        }
                        Const.STATE_DISCONNECTED -> {
                            CoroutineScope(Dispatchers.IO).launch { onEnded() }
                        }
                    }
                }
                connect(context, sessionId, serverSecret,
                    object : SharingEngine.CompletionHandler {
                        override fun onComplete(success: Boolean, errorReason: String?) {
                            if (success) {
                                Log.d(TAG, "Sharing engine connected")
                            } else {
                                Log.e(TAG, "Sharing engine failed: $errorReason")
                                CoroutineScope(Dispatchers.IO).launch { onEnded() }
                            }
                        }
                    })
            }
        })
    }

    fun stop() {
        Handler(Looper.getMainLooper()).post(Runnable {
            sharingEngine?.disconnect(context, object : SharingEngine.CompletionHandler {
                override fun onComplete(success: Boolean, errorReason: String?) {
                    Log.d(TAG, "Disconnected: success=$success error=$errorReason")
                }
            })
            sharingEngine = null
            engineLaunched = false
        })
    }
}
