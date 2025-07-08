package cn.vove7.andro_accessibility_api.demo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cn.vove7.andro_accessibility_api.demo.service.ScreenCaptureService
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import timber.log.Timber

class WebSocketService : Service() {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun onCreate() {
        super.onCreate()
        connect()
    }

    private fun connect() {
        val request = Request.Builder()
            .url("ws://192.168.10.39:9933/websocket/web/1")
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
        ScreenCaptureService.captureScreen(this) { base64 ->
            if (base64 == null) {
                Timber.e("capture screen failed")
                return@captureScreen
            }
            val obj = JSONObject()
            obj.put("type", 1)
            obj.put("base64", base64)
            webSocket?.send(obj.toString())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}