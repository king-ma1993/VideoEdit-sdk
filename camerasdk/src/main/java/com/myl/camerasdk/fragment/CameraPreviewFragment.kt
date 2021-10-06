package com.myl.camerasdk.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myl.camerasdk.camera.CameraParam
import com.myl.camerasdk.databinding.FragmentCameraPreviewBinding

class CameraPreviewFragment : Fragment() {

    // 预览参数
    private var mCameraParam: CameraParam = CameraParam.instance
    private var mMainHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainHandler.removeCallbacksAndMessages(null)
    }


}