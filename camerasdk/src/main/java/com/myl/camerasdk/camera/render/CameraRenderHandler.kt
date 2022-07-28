package com.myl.camerasdk.camera.render

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference
import java.util.*

/**
 * 渲染器Handler
 */
class CameraRenderHandler(looper: Looper, renderer: CameraRenderer?) : Handler(looper) {
    // 渲染事件处理队列
    private val mEventQueue = ArrayList<Runnable>()
    private val mWeakRender: WeakReference<CameraRenderer?> = WeakReference(renderer)
    override fun handleMessage(msg: Message) {
        if (mWeakRender.get() == null) {
            return
        }
        handleQueueEvent()
        val renderer = mWeakRender.get()
    }

    /**
     * 处理优先的队列事件
     */
    fun handleQueueEvent() {
        synchronized(this) {
            var runnable: Runnable
            while (mEventQueue.isNotEmpty()) {
                runnable = mEventQueue.removeAt(0)
                if (runnable != null) {
                    runnable.run()
                }
            }
        }
    }

    /**
     * 入队事件
     * @param runnable
     */
    fun queueEvent(runnable: Runnable?) {
        requireNotNull(runnable) { "runnable must not be null" }
        synchronized(this) {
            mEventQueue.add(runnable)
            notifyAll()
        }
    }

    companion object {
        const val MSG_INIT = 0x01 // 初始化
        const val MSG_DISPLAY_CHANGE = 0x02 // 显示发生变化
        const val MSG_DESTROY = 0x03 // 销毁
        const val MSG_RENDER = 0x04 // 渲染
        const val MSG_CHANGE_FILTER = 0x05 // 切换滤镜
        const val MSG_CHANGE_MAKEUP = 0x06 // 切换彩妆
        const val MSG_CHANGE_RESOURCE = 0x07 // 切换贴纸资源
        const val MSG_CHANGE_EDGE_BLUR = 0x08 // 边框模糊功能
    }

}