package com.example.autoclicker

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
                Toast.makeText(this, "請開啟懸浮視窗權限", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 檢查無障礙實體連線是否就緒
            if (AutoClickService.instance == null) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(
                    this,
                    "無障礙連線已中斷，請在設定中將「連點服務」關閉後再重新開啟一次！",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 3. 啟動懸浮視窗
            val inputSeconds = etInterval.text.toString().toDoubleOrNull() ?: 0.1
            val intervalMs = (maxOf(0.1, inputSeconds) * 1000).toLong()

            val serviceIntent = Intent(this, FloatingService::class.java).apply {
                putExtra("INTERVAL_MS", intervalMs)
            }
            startService(serviceIntent)
        }
    }
}