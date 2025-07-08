package cn.vove7.andro_accessibility_api.demo.service
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import java.io.ByteArrayOutputStream

class ScreenCaptureHelper(private val activity: Activity) {
    companion object {
        private const val REQUEST_SCREENSHOT = 1001
    }

    private val mediaProjectionManager =
        activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    fun requestScreenCapture() {
        activity.startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_SCREENSHOT
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_SCREENSHOT && resultCode == Activity.RESULT_OK && data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            return true
        }
        return false
    }

    fun captureScreen(onCaptured: (String?) -> Unit) {
        if (mediaProjection == null) {
            Log.e("ScreenCaptureHelper", "MediaProjection is null. Did you call requestScreenCapture()?")
            onCaptured(null)
            return
        }

        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, 1, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        Handler(HandlerThread("PixelCopyHandler").apply { start() }.looper).postDelayed({
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(imageReader!!.surface, bitmap, { result ->
                if (result == PixelCopy.SUCCESS) {
                    onCaptured(bitmapToBase64(bitmap))
                } else {
                    onCaptured(null)
                }
                release()
            }, Handler(activity.mainLooper))
        }, 500)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun release() {
        virtualDisplay?.release()
        mediaProjection?.stop()
        mediaProjection = null
    }
}
