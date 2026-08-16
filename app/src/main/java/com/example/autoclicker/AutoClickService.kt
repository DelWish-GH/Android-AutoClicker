package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AutoClickService : AccessibilityService() {

    companion object {
        var instance: AutoClickService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return true // 允許後續自動 rebind
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun simulateClick(x: Float, y: Float, onComplete: () -> Unit = {}) {
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x, y)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        val isDispatched = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Toast.makeText(applicationContext, "點擊手勢被系統取消", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }, null)

        if (!isDispatched) {
            Toast.makeText(applicationContext, "手勢發送失敗，請重新開啟服務", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }
}