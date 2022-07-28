package com.myl.camerasdk.camera.render

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.view.Surface

/**
 * 相机渲染器
 */
class CameraRenderer : Thread(TAG) {
    private val mLooper: Looper? = null

    private val handler: CameraRenderHandler by lazy {
        CameraRenderHandler(getLooper(), this)
    }

    companion object {
         private const val TAG = "CameraRenderer"
    }

    /**
     * 绑定Surface
     * @param surface
     */
    fun onSurfaceCreated(surface: Surface?) {
        handler.sendMessage(handler.obtainMessage(CameraRenderHandler.MSG_INIT, surface))
    }

    /**
     * 绑定SurfaceTexture
     * @param surfaceTexture
     */
    fun onSurfaceCreated(surfaceTexture: SurfaceTexture?) {
        handler.sendMessage(handler.obtainMessage(CameraRenderHandler.MSG_INIT, surfaceTexture))
    }

    /**
     * 设置预览大小
     * @param width
     * @param height
     */
    fun onSurfaceChanged(width: Int, height: Int) {
        val handler: Handler = handler
        handler.sendMessage(handler.obtainMessage(CameraRenderHandler.MSG_DISPLAY_CHANGE, width, height))
    }

    /**
     * 解绑Surface
     */
    fun onSurfaceDestroyed() {
        val handler: Handler = handler
        handler.sendMessage(handler.obtainMessage(CameraRenderHandler.MSG_DESTROY))
    }

    /**
     * 获取当前的Looper
     * @return
     */
    private fun getLooper(): Looper? {
        if (!isAlive) {
            return null
        }
        synchronized(this) {
            while (isAlive && mLooper == null) {
                try {
                    wait()
                } catch (e: InterruptedException) {
                }
            }
        }
        return mLooper
    }

}