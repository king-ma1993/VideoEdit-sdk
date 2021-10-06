package com.myl.camerasdk.listener

import android.graphics.SurfaceTexture

/**
 * SurfaceTexture准备成功监听器
 */
interface OnSurfaceTextureListener {
    fun onSurfaceTexturePrepared(surfaceTexture: SurfaceTexture?)
}