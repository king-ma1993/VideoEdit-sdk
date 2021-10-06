package com.myl.camerasdk.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.myl.camerasdk.R
import com.myl.camerasdk.databinding.ActivityCameraBinding
import com.myl.camerasdk.fragment.CameraPreviewFragment

class CameraActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_CAMERA = "fragment_camera"
    }

    private lateinit var activityCameraBinding: ActivityCameraBinding
    private lateinit var mPreviewFragment: CameraPreviewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCameraBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(activityCameraBinding.root)
        if (null == savedInstanceState && mPreviewFragment == null) {
            mPreviewFragment = CameraPreviewFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, mPreviewFragment, FRAGMENT_CAMERA)
                .commit()
        }
    }
}