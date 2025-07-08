package cn.vove7.andro_accessibility_api.demo.service

import android.util.Log
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope
import android.graphics.Path
import android.accessibilityservice.GestureDescription
import android.os.Build

/**
 * # MyAccessibilityService
 *
 * Created on 2020/6/10
 * @author Vove
 */
class AppAccessibilityService : AccessibilityApi() {
    //启用 页面更新 回调
    override val enableListenPageUpdate: Boolean = true

    override fun onCreate() {
        //must set
        baseService = this
        super.onCreate()
        AccessibilityHelper.setService(this)
    }

    override fun onDestroy() {
        //must set
        baseService = null
        super.onDestroy()
    }

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {
        Log.d(TAG, "onPageUpdate: $currentScope")
    }

    companion object {
        private const val TAG = "MyAccessibilityService"
    }
    fun clickOnScreen(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            path.moveTo(x.toFloat(), y.toFloat())

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()

            dispatchGesture(gesture, null, null)
        }
    }
    fun slideOnScreen(x1: Int, y1: Int, x2: Int, y2: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            path.moveTo(x1.toFloat(), y1.toFloat())
            path.lineTo(x2.toFloat(), y2.toFloat())
//            path.close()

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
                .build()

            dispatchGesture(gesture, null, null)
        }
    }
}