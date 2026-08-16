package com.example.autoclicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val prefsName = "AutoClickPrefs"
    private val keyInterval = "last_interval"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etInterval = findViewById<EditText>(R.id.etInterval)
        val btnOpenOverlay = findViewById<Button>(R.id.btnOpenOverlay)

        // 讀取上次儲存的秒數（預設為 0.1）
        val sharedPrefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedInterval = sharedPrefs.getString(keyInterval, "0.1")
        etInterval.setText(savedInterval)

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

            // 2. 檢查無障礙連線狀態
            if (AutoClickService.instance == null) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(
                    this,
                    "無障礙連線中斷，請在設定中將 AutoClick 關閉後重新開啟",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 3. 取得、校驗並儲存秒數
            val inputText = etInterval.text.toString().trim()
            val inputSeconds = inputText.toDoubleOrNull() ?: 0.1
            val validSeconds = maxOf(0.1, inputSeconds)

            // 將本次輸入的數值持久化保存
            sharedPrefs.edit().putString(keyInterval, inputText.ifEmpty { "0.1" }).apply()

            val intervalMs = (validSeconds * 1000).toLong()

            val serviceIntent = Intent(this, FloatingService::class.java).apply {
                putExtra("INTERVAL_MS", intervalMs)
            }
            startService(serviceIntent)
        }
    }
}