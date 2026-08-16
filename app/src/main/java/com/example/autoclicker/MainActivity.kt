package com.example.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "請先開啟懸浮窗權限", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (AutoClickService.instance == null) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(this, "請在無障礙設定中開啟「連點無障礙點擊服務」", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val inputSeconds = etInterval.text.toString().toDoubleOrNull() ?: 0.1
            val intervalMs = (maxOf(0.1, inputSeconds) * 1000).toLong()

            val serviceIntent = Intent(this, FloatingService::class.java).apply {
                putExtra("INTERVAL_MS", intervalMs)
            }
            startService(serviceIntent)
        }
    }
}