package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun simulateClick(x: Float, y: Float, onComplete: () -> Unit = {}) {
        // 必須包含 moveTo 與 lineTo 才能構成有效的點擊路徑
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x, y)
        }

        // 點擊觸控維持 100ms 確保系統底層完整觸發 Down -> Up
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        val isDispatched = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Toast.makeText(applicationContext, "點擊被系統取消，請檢查權限", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }, null)

        if (!isDispatched) {
            Toast.makeText(applicationContext, "手勢發送失敗 (dispatchGesture false)", Toast.LENGTH_SHORT).show()
        }
    }
}