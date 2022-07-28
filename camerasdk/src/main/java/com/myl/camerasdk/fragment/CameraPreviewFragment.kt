package com.myl.camerasdk.fragment

import android.app.Fragment
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myl.camerasdk.R
import com.myl.camerasdk.camera.CameraParam
import com.myl.camerasdk.databinding.FragmentCameraPreviewBinding
import com.myl.camerasdk.viewmodel.CameraPreviewViewModel
import com.myl.camerasdk.widget.CameraTextureView

class CameraPreviewFragment : Fragment(R.layout.fragment_camera_preview) {


    private lateinit var cameraPreviewBinding: FragmentCameraPreviewBinding
    // 预览参数
    private var mCameraParam = CameraParam
    private var mMainHandler: Handler = Handler(Looper.getMainLooper())
    private var mCameraTextureView: CameraTextureView? = null
    private val cameraPreviewViewModel: CameraPreviewViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraPreviewBinding = FragmentCameraPreviewBinding.bind(view)
        initView(view)
    }

    private fun initView(view: View) {
        initPreviewSurface()

    }

    private fun initPreviewSurface() {
        mCameraTextureView = activity?.let { CameraTextureView(it) }
        cameraPreviewBinding.layoutCameraPreview.addView(mCameraTextureView)
        mCameraTextureView?.surfaceTextureListener = mSurfaceTextureListener
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainHandler.removeCallbacksAndMessages(null)
    }

    // ---------------------------- TextureView SurfaceTexture监听 ---------------------------------
    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            cameraPreviewViewModel.onSurfaceCreated(surface)
            cameraPreviewViewModel.onSurfaceChanged(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            cameraPreviewViewModel.onSurfaceChanged(width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            cameraPreviewViewModel.onSurfaceDestroyed()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

}