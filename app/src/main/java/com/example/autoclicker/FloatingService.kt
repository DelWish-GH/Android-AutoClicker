package com.example.autoclicker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.*

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var intervalMs: Long = 100L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intervalMs = intent?.getLongExtra("INTERVAL_MS", 100L) ?: 100L
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
    }

    private fun createFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 150
            y = 300
        }

        windowManager.addView(floatingView, params)

        val dragHandle = floatingView.findViewById<View>(R.id.dragHandle)
        val aimPoint = floatingView.findViewById<ImageView>(R.id.aimPoint)
        val btnStart = floatingView.findViewById<Button>(R.id.btnStart)
        val btnClose = floatingView.findViewById<Button>(R.id.btnClose)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }

        btnStart.setOnClickListener {
            val clickService = AutoClickService.instance
            if (clickService == null) {
                Toast.makeText(this, "無障礙點擊服務未啟動，請重新開啟權限", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val location = IntArray(2)
            aimPoint.getLocationOnScreen(location)
            val targetX = location[0] + (aimPoint.width / 2f)
            val targetY = location[1] + (aimPoint.height / 2f)

            btnStart.isEnabled = false
            btnStart.text = "執行中"

            serviceScope.launch {
                // 1. 啟用觸控穿透 + 暫時隱形
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                windowManager.updateViewLayout(floatingView, params)
                floatingView.visibility = View.INVISIBLE
                delay(100)

                // 2. 第 1 次點擊
                clickService.simulateClick(targetX, targetY)

                // 3. 等待設定之間隔 (毫秒)
                delay(intervalMs)

                // 4. 第 2 次點擊
                clickService.simulateClick(targetX, targetY)
                delay(150)

                // 5. 恢復懸浮窗顯示與觸控響應
                floatingView.visibility = View.VISIBLE
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                windowManager.updateViewLayout(floatingView, params)

                btnStart.isEnabled = true
                btnStart.text = "開始"

                Toast.makeText(
                    this@FloatingService,
                    "點擊完成！座標：(${targetX.toInt()}, ${targetY.toInt()})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnClose.setOnClickListener {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}