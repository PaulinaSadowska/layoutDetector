package com.paulinasadowska.layoutdetector

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.service.voice.VoiceInteractionSession
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import java.util.*


class MyAssistantSession(context: Context?) : VoiceInteractionSession(context) {

    var content: RelativeLayout? = null

    override fun onCreateContentView(): View {
        val view = layoutInflater.inflate(R.layout.voice_interaction_session, null);
        content = view.findViewById(R.id.inspection_content)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onHandleAssist(state: AssistState) {
        state.assistStructure?.getWindowNodeAt(0)?.rootViewNode?.let {
            drawRects(it)
        }
        super.onHandleAssist(state)
    }

    private fun drawRects(root: AssistStructure.ViewNode) {
        val deque: Deque<NodeWrapper> = ArrayDeque()
        deque.add(NodeWrapper(root, 0, 0, 0))
        // val locationOnScreen = IntArray(2)
        // layout?.getLocationOnScreen(locationOnScreen)
        var startConting = false
        while (!deque.isEmpty()) {
            val aa = deque.removeFirst()
            val node = aa.node
            if (node.idEntry == "mboxHolder") {
                startConting = true
            }
            for (i in 0 until node.childCount) {
                deque.addLast(NodeWrapper(node.getChildAt(i), aa.insetTop + node.top, aa.insetLeft + node.left, if (startConting) aa.depth + 1 else 0))
            }
            if (node.childCount == 0 && aa.depth > 0) {
                val view = View(context).apply {
                    layoutParams = RelativeLayout.LayoutParams(node.width, node.height).also {
                        it.topMargin = node.top + aa.insetTop
                        it.marginStart = node.left + aa.insetLeft
                    }
                    setBackgroundResource(getRes(aa.depth))

                }
                content?.addView(view)

            }
        }
    }

    private fun getRes(depth: Int): Int {
        return when (depth) {
            1 -> R.drawable.borderblue
            2 -> R.drawable.bordergreen
            3 -> R.drawable.borderyellow
            4 -> R.drawable.borderorange
            5 -> R.drawable.borderred
            else -> R.drawable.borderred
        }
    }

    data class NodeWrapper(
            val node: AssistStructure.ViewNode,
            val insetTop: Int,
            val insetLeft: Int,
            val depth: Int
    )

}