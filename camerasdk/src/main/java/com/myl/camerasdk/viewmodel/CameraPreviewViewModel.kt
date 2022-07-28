package com.myl.camerasdk.viewmodel

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import com.myl.camerasdk.camera.render.CameraRenderer

class CameraPreviewViewModel : ViewModel() {

    // 渲染器
    private val mCameraRenderer: CameraRenderer = CameraRenderer()

    fun openCamera() {

    }


    fun onSurfaceCreated(surfaceTexture: SurfaceTexture?) {
        mCameraRenderer.onSurfaceCreated(surfaceTexture)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        mCameraRenderer.onSurfaceChanged(width, height)
    }

    fun onSurfaceDestroyed() {
        mCameraRenderer.onSurfaceDestroyed()
    }


}
