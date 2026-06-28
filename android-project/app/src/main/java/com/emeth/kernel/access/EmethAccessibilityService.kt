package com.emeth.kernel.access

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.emeth.kernel.spine.AirOsSpine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ScreenNode(
    val index: Int,
    val text: String,
    val className: String?,
    val clickable: Boolean,
    val editable: Boolean,
    val scrollable: Boolean,
    val bounds: String
)

data class ScreenSnapshot(
    val packageName: String?,
    val windowTitle: String?,
    val nodes: List<ScreenNode>
)

class EmethAccessibilityService : AccessibilityService() {
    private val spineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var airOsSpine: AirOsSpine
    private var lastUiPublishAt = 0L

    override fun onServiceConnected() {
        instance = this
        airOsSpine = AirOsSpine(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType ?: return
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastUiPublishAt < 750L) return
        lastUiPublishAt = now
        val current = snapshot(maxNodes = 60) ?: return

        spineScope.launch {
            val nodes = JSONArray()
            current.nodes.forEach { node ->
                nodes.put(
                    JSONObject()
                        .put("id", SharedIdForUi.node(current.packageName, node))
                        .put("index", node.index)
                        .put("text", node.text)
                        .put("class", node.className)
                        .put("clickable", node.clickable)
                        .put("editable", node.editable)
                        .put("scrollable", node.scrollable)
                        .put("bounds", node.bounds)
                )
            }
            airOsSpine.publishUiMap(
                key = "screen.active",
                payload = JSONObject()
                    .put("package", current.packageName)
                    .put("title", current.windowTitle)
                    .put("nodes", nodes)
                    .put("capturedAt", now)
            )
            airOsSpine.emitBehavior(
                kind = "ui_observed",
                payload = JSONObject()
                    .put("package", current.packageName)
                    .put("nodeCount", current.nodes.size)
            )
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance == this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instance: EmethAccessibilityService? = null

        fun isEnabled(): Boolean = instance != null

        fun performGlobal(action: Int): Boolean {
            return instance?.performGlobalAction(action) == true
        }

        fun tapText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val matches = root.findAccessibilityNodeInfosByText(text)
            val target = matches.firstOrNull { it.isVisibleToUser } ?: return false
            return target.performClickUpTree()
        }

        fun waitForAndClick(text: String, timeoutMs: Long = 5000): Boolean {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                if (tapText(text)) return true
                Thread.sleep(250)
            }
            return false
        }
        
        fun longTapText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val matches = root.findAccessibilityNodeInfosByText(text)
            val target = matches.firstOrNull { it.isVisibleToUser } ?: return false
            return target.performLongClickUpTree()
        }

        fun tapContentDescription(desc: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val nodes = mutableListOf<AccessibilityNodeInfo>()
            fun collect(node: AccessibilityNodeInfo) {
                if (node.isVisibleToUser && node.contentDescription?.toString() == desc) {
                    nodes.add(node)
                }
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { collect(it) }
                }
            }
            collect(root)
            val target = nodes.firstOrNull() ?: return false
            return target.performClickUpTree()
        }

        fun snapshot(maxNodes: Int = 40): ScreenSnapshot? {
            val service = instance ?: return null
            val root = service.rootInActiveWindow ?: return null
            val nodes = mutableListOf<ScreenNode>()
            root.collectVisibleNodes(nodes, maxNodes)
            return ScreenSnapshot(
                packageName = root.packageName?.toString(),
                windowTitle = root.paneTitle?.toString(),
                nodes = nodes
            )
        }

        fun tapIndex(index: Int): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val nodes = mutableListOf<IndexedAccessibilityNode>()
            root.collectIndexedAccessibilityNodes(nodes, 80)
            val target = nodes.firstOrNull { it.index == index }?.node ?: return false
            return target.performClickUpTree()
        }

        fun typeText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focused != null && focused.isEditable) {
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }

            val editable = root.findFirstEditable() ?: return false
            editable.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        fun scrollForward(): Boolean {
            return instance?.rootInActiveWindow?.findScrollable()
                ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) == true
        }

        fun scrollBackward(): Boolean {
            return instance?.rootInActiveWindow?.findScrollable()
                ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) == true
        }

        private fun AccessibilityNodeInfo.performClickUpTree(): Boolean {
            var node: AccessibilityNodeInfo? = this
            while (node != null) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
                node = node.parent
            }
            return false
        }
        
        private fun AccessibilityNodeInfo.performLongClickUpTree(): Boolean {
            var node: AccessibilityNodeInfo? = this
            while (node != null) {
                if (node.isLongClickable && node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)) return true
                node = node.parent
            }
            return false
        }

        private fun AccessibilityNodeInfo.findFirstEditable(): AccessibilityNodeInfo? {
            if (isEditable && isVisibleToUser) return this
            for (i in 0 until childCount) {
                val match = getChild(i)?.findFirstEditable()
                if (match != null) return match
            }
            return null
        }

        private fun AccessibilityNodeInfo.findScrollable(): AccessibilityNodeInfo? {
            if (isScrollable && isVisibleToUser) return this
            for (i in 0 until childCount) {
                val match = getChild(i)?.findScrollable()
                if (match != null) return match
            }
            return null
        }

        private fun AccessibilityNodeInfo.collectVisibleNodes(result: MutableList<ScreenNode>, maxNodes: Int) {
            if (result.size >= maxNodes) return
            val label = nodeLabel()
            if (isVisibleToUser && label.isNotBlank()) {
                result += ScreenNode(
                    index = result.size + 1,
                    text = label,
                    className = className?.toString(),
                    clickable = isClickable || hasClickableParent(),
                    editable = isEditable,
                    scrollable = isScrollable,
                    bounds = boundsString()
                )
            }
            for (i in 0 until childCount) {
                getChild(i)?.collectVisibleNodes(result, maxNodes)
                if (result.size >= maxNodes) return
            }
        }

        private fun AccessibilityNodeInfo.collectIndexedAccessibilityNodes(result: MutableList<IndexedAccessibilityNode>, maxNodes: Int) {
            if (result.size >= maxNodes) return
            if (isVisibleToUser && nodeLabel().isNotBlank()) {
                result += IndexedAccessibilityNode(result.size + 1, this)
            }
            for (i in 0 until childCount) {
                getChild(i)?.collectIndexedAccessibilityNodes(result, maxNodes)
                if (result.size >= maxNodes) return
            }
        }

        private fun AccessibilityNodeInfo.nodeLabel(): String {
            return listOfNotNull(
                text?.toString(),
                contentDescription?.toString(),
                hintText?.toString()
            ).firstOrNull { it.isNotBlank() }.orEmpty().trim()
        }

        private fun AccessibilityNodeInfo.boundsString(): String {
            val rect = Rect()
            getBoundsInScreen(rect)
            return "${rect.left},${rect.top},${rect.right},${rect.bottom}"
        }

        private fun AccessibilityNodeInfo.hasClickableParent(): Boolean {
            var node = parent
            while (node != null) {
                if (node.isClickable) return true
                node = node.parent
            }
            return false
        }

        private data class IndexedAccessibilityNode(
            val index: Int,
            val node: AccessibilityNodeInfo
        )
    }
}

private object SharedIdForUi {
    fun node(packageName: String?, node: ScreenNode): String {
        return com.emeth.kernel.spine.SharedId.stableId(
            "ui_node",
            packageName.orEmpty() + ":" + node.className + ":" + node.text + ":" + node.bounds
        )
    }
}
