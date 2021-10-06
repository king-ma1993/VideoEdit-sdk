package com.myl.camerasdk.listener

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.myl.camerasdk.utils.ImageConvert
import com.myl.camerasdk.utils.ImageConvert.getDataFromImage

/**
 * 预览帧分析器
 */
class PreviewCallbackAnalyzer(private val mPreviewCallback: PreviewCallback?) :
    ImageAnalysis.Analyzer {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val start = System.currentTimeMillis()
        if (VERBOSE) {
            Log.d(
                TAG, "analyze: timestamp - " + image.imageInfo.timestamp + ", " +
                        "orientation - " + image.imageInfo.rotationDegrees + ", imageFormat" +
                        " - " + image.format
            )
        }
        if (mPreviewCallback != null && image.image != null) {
            val data = getDataFromImage(
                image.image!!,
                ImageConvert.COLOR_FORMAT_NV21
            )
            if (data != null) {
                mPreviewCallback.onPreviewFrame(data)
            }
        }
        // 使用完需要释放，否则下一次不会回调了
        image.close()
        if (VERBOSE) {
            Log.d(TAG, "convert cost time - " + (System.currentTimeMillis() - start))
        }
    }

    companion object {
        private const val TAG = "PreviewCallbackAnalyzer"
        private const val VERBOSE = false
    }
}