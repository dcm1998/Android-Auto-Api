package cn.vove7.andro_accessibility_api.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.demo.actions.*
import cn.vove7.andro_accessibility_api.demo.databinding.ActivityMainBinding
import cn.vove7.auto.core.AutoApi
import cn.vove7.auto.core.utils.jumpAccessibilityServiceSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

import java.io.ByteArrayOutputStream
import android.util.Base64
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import cn.vove7.andro_accessibility_api.demo.service.ScreenCaptureHelper
import cn.vove7.andro_accessibility_api.demo.service.ScreenCaptureService

class MainActivity : AppCompatActivity() {
    public lateinit var screenCaptureHelper: ScreenCaptureHelper
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            toast( "点击坐标: x=$x, y=$y")
        }
        return super.dispatchTouchEvent(event) // 继续分发事件
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 请求截图权限
//        screenCaptureHelper = ScreenCaptureHelper(this)
//        screenCaptureHelper.requestScreenCapture()
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1001)

        val actions = mutableListOf(
            clickNavigatorAction(),
            BaseNavigatorAction(),
//            PickScreenText(),
//            SiblingTestAction(),
//            DrawableAction(),
//            WaitAppAction(),
//            SelectTextAction(),
//            ViewFinderWithLambda(),
//            TextMatchAction(),
//            ClickTextAction(),
//            TraverseAllAction(),
//            SmartFinderAction(),
//            CoroutineStopAction(),
//            ToStringTestAction(),
//            InstrumentationSendKeyAction(),
//            InstrumentationSendTextAction(),
//            InstrumentationInjectInputEventAction(),
//            InstrumentationShotScreenAction(),
//            SendImeAction(),
//            ContinueGestureAction(),
//            object : Action() {
//                override val name = "Stop"
//                override suspend fun run(act: ComponentActivity) {
//                    actionJob?.cancel()
//                }
//            }
        )

        binding.listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, actions)
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            onActionClick(actions[position])
        }
        binding.acsCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !AccessibilityApi.isServiceEnable) {
                buttonView.isChecked = false
                jumpAccessibilityServiceSettings(AccessibilityApi.BASE_SERVICE_CLS)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.acsCb.isChecked = AccessibilityApi.isServiceEnable
        binding.acsCb.isEnabled = AutoApi.serviceType != AutoApi.SERVICE_TYPE_INSTRUMENTATION

        binding.workMode.text = "工作模式：${
            mapOf(
                AutoApi.SERVICE_TYPE_NONE to "无",
                AutoApi.SERVICE_TYPE_ACCESSIBILITY to "无障碍",
                AutoApi.SERVICE_TYPE_INSTRUMENTATION to "Instrumentation",
            )[AutoApi.serviceType]
        } "
    }

    var actionJob: Job? = null

    private fun onActionClick(action: Action) {
        if (action.name == "Stop") {
            actionJob?.cancel()
            return
        }
        if (actionJob?.isCompleted.let { it != null && !it }) {
            toast("有正在运行的任务")
            return
        }
        actionJob = launchWithExpHandler {
            action.run(this@MainActivity)
        }
        actionJob?.invokeOnCompletion {
            if (it is CancellationException) {
                toast("取消执行")
            } else if (it == null) {
                toast("执行结束")
            }
        }
    }

    override fun onDestroy() {
        actionJob?.cancel()
        super.onDestroy()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
//            ScreenCaptureService.mediaProjection =
//                mediaProjectionManager.getMediaProjection(resultCode, data)
            val intent = Intent(this, ScreenCaptureService::class.java)
            intent.putExtra("resultCode", resultCode)
            intent.putExtra("data", data)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}
