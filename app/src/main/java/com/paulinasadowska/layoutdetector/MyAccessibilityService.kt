package com.paulinasadowska.layoutdetector

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import java.util.*


class MyAccessibilityService : AccessibilityService() {

    private var layout: FrameLayout? = null
    private val scrollButton: Button by lazy { layout!!.findViewById<View>(R.id.scroll) as Button }
    private val drawButton: Button by lazy { layout!!.findViewById<View>(R.id.draw) as Button }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onServiceConnected() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layout = FrameLayout(this)
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // this is to enable the notification to receive touch events
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // Draws over status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
        }
        layout?.setBackgroundColor(Color.argb(20, 255, 0, 0))
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, layout)
        windowManager.addView(layout, layoutParams)
        layout?.fitsSystemWindows = true

        configureScrollButton()
        configureDraw()

    }

    private fun configureDraw() {
        drawButton.setOnClickListener {
            drawRects(rootInActiveWindow)
        }
    }

    private fun drawRects(root: AccessibilityNodeInfo) {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        val locationOnScreen = IntArray(2)
        layout?.getLocationOnScreen(locationOnScreen)
        while (!deque.isEmpty()) {
            val node: AccessibilityNodeInfo = deque.removeFirst()
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
            if (node.childCount == 0) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                val view = View(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(rect.width(), rect.height()).also {
                        it.topMargin = rect.top - locationOnScreen[1]
                        it.marginStart = rect.left
                    }
                    setBackgroundResource(R.drawable.borderred)

                }
                layout?.addView(view)
                Log.d("test", "todo draw rect at ${rect.bottom} ${rect.top} ${node.text}")
            }
        }
    }


    private fun configureScrollButton() {
        scrollButton.setOnClickListener {
            val scrollable = findScrollableNode(rootInActiveWindow)
            scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node: AccessibilityNodeInfo = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("EVENT!!!", "event: ${event?.className}")
    }
}
