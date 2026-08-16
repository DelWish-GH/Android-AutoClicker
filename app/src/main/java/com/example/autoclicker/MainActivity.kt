package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etInterval = findViewById<EditText>(R.id.etInterval)
        val btnOpenOverlay = findViewById<Button>(R.id.btnOpenOverlay)

        btnOpenOverlay.setOnClickListener {
            // 1. 檢查懸浮視窗權限
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "請先開啟懸浮窗權限", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 透過系統底層設定查詢無障礙是否真正開啟
            if (!isAccessibilityServiceEnabled(this, AutoClickService::class.java)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(this, "請在無障礙設定中開啟「連點無障礙點擊服務」", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 3. 取得間隔時間並啟動懸浮窗
            val inputSeconds = etInterval.text.toString().toDoubleOrNull() ?: 0.1
            val intervalMs = (maxOf(0.1, inputSeconds) * 1000).toLong()

            val serviceIntent = Intent(this, FloatingService::class.java).apply {
                putExtra("INTERVAL_MS", intervalMs)
            }
            startService(serviceIntent)
        }
    }

    // 正確查詢 Android 系統無障礙授權狀態的方法
    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
        val expectedComponentName = ComponentName(context, service)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }
}