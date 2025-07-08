package cn.vove7.andro_accessibility_api.demo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Base64
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import timber.log.Timber
import java.io.File

class WebSocketService : Service() {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun onCreate() {
        super.onCreate()
        connect()
    }

    private fun connect() {
        val request = Request.Builder()
            .url("ws://192.168.2.114:9933/ws/1")
            .build()
        webSocket = client.newWebSocket(request, socketListener)
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.i("WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("onMessage: $text")
            runCatching {
                val obj = JSONObject(text)
                if (obj.optInt("type") == 2) {
                    captureAndSend()
                }
            }.onFailure { Timber.e(it) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            onMessage(webSocket, bytes.utf8())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
        }
    }

    private fun captureAndSend() {
        val base64 = captureScreenBase64() ?: return
        val obj = JSONObject()
        obj.put("type", 1)
        obj.put("base64", base64)
        webSocket?.send(obj.toString())
    }

    private fun captureScreenBase64(): String? {
        return runCatching {
            val file = File(cacheDir, "ws_screenshot.png")
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "screencap -p ${file.absolutePath}"))
            process.waitFor()
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.onFailure { Timber.e(it) }.getOrNull()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
