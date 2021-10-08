package com.myl.camerasdk.camera

import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.HandlerThread
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.*

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class CameraController(private val activity: Activity) : BaseCameraController(),
    Camera.PreviewCallback {

    // 摄像头id
    private var mCameraId = 0

    // 相机输出的SurfaceTexture
    private var mOutputTexture: SurfaceTexture? = null
    private var mOutputThread: HandlerThread? = null

    // 期望的fps
    private val mExpectFps = CameraParam.DESIRED_PREVIEW_FPS

    init {
        mCameraId =
            if (CameraApi.hasFrontCamera(activity)) Camera.CameraInfo.CAMERA_FACING_FRONT
            else Camera.CameraInfo.CAMERA_FACING_BACK
    }

    // 相机对象
    private var mCamera: Camera? = null

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun openCamera() {
        closeCamera()
        mCamera = Camera.open(mCameraId)
        if (mCamera == null) {
            throw RuntimeException("Unable to open camera")
        }
        val cameraParam: CameraParam = CameraParam.instance
        cameraParam.cameraId = mCameraId
        val parameters = mCamera?.parameters
        cameraParam.supportFlash = checkSupportFlashLight(parameters)
        cameraParam.previewFps =
            chooseFixedPreviewFps(parameters, mExpectFps * 1000)
        parameters?.setRecordingHint(true)
        // 后置摄像头自动对焦
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK
            && supportAutoFocusFeature(parameters)
        ) {
            mCamera!!.cancelAutoFocus()
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        }
        mCamera!!.parameters = parameters
        setPreviewSize(mCamera, mPreviewWidth, mPreviewHeight)
        setPictureSize(mCamera, mPreviewWidth, mPreviewHeight)
        mOrientation = calculateCameraPreviewOrientation(mActivity)
        mCamera!!.setDisplayOrientation(mOrientation)
        releaseSurfaceTexture()
        mOutputTexture = createDetachedSurfaceTexture()
        try {
            mCamera!!.setPreviewTexture(mOutputTexture)
            mCamera!!.setPreviewCallback(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mCamera!!.startPreview()
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener!!.onSurfaceTexturePrepared(mOutputTexture)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun closeCamera() {
        mCamera?.apply {
            setPreviewCallback(null)
            setPreviewCallbackWithBuffer(null)
            addCallbackBuffer(null)
            stopPreview()
            release()
        }
        releaseSurfaceTexture()
    }

    /**
     * 检查摄像头(前置/后置)是否支持闪光灯
     * @param parameters 摄像头参数
     * @return
     */
    private fun checkSupportFlashLight(parameters: Camera.Parameters?): Boolean {
        if (parameters?.flashMode == null) {
            return false
        }
        val supportedFlashModes = parameters.supportedFlashModes
        return !(supportedFlashModes == null || supportedFlashModes.isEmpty()
                || (supportedFlashModes.size == 1
                && supportedFlashModes[0] == Camera.Parameters.FLASH_MODE_OFF))
    }

    /**
     * 释放资源
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun releaseSurfaceTexture() {
        mOutputTexture?.release()
        mOutputTexture = null

        mOutputThread?.quitSafely()
        mOutputThread = null
    }

    override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
        TODO("Not yet implemented")
    }


    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private fun setPreviewSize(camera: Camera, expectWidth: Int, expectHeight: Int) {
        val parameters = camera.parameters
        val size: Camera.Size = calculatePerfectSize(
            parameters.supportedPreviewSizes,
            expectWidth, expectHeight, CalculateType.Lower
        )
        parameters.setPreviewSize(size.width, size.height)
        mPreviewWidth = size.width
        mPreviewHeight = size.height
        camera.parameters = parameters
    }

    /**
     * 设置拍摄的照片大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private fun setPictureSize(camera: Camera, expectWidth: Int, expectHeight: Int) {
        val parameters = camera.parameters
        val size: Camera.Size? = calculatePerfectSize(
            parameters.supportedPictureSizes,
            expectWidth, expectHeight, CalculateType.Max
        )
        parameters.setPictureSize(size?.width ?: 0, size?.height ?: 0)
        camera.parameters = parameters
    }

    /**
     * 计算最完美的Size
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    private fun calculatePerfectSize(
        sizes: List<Camera.Size>, expectWidth: Int,
        expectHeight: Int, calculateType: CalculateType
    ): Camera.Size {
        sortList(sizes) // 根据宽度进行排序

        // 根据当前期望的宽高判定
        val bigEnough: MutableList<Camera.Size> = ArrayList()
        val noBigEnough: MutableList<Camera.Size> = ArrayList()
        for (size in sizes) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width > expectWidth && size.height > expectHeight) {
                    bigEnough.add(size)
                } else {
                    noBigEnough.add(size)
                }
            }
        }
        // 根据计算类型判断怎么如何计算尺寸
        var perfectSize: Camera.Size? = null
        when (calculateType) {
            CalculateType.Min ->                 // 不大于期望值的分辨率列表有可能为空或者只有一个的情况，
                // Collections.min会因越界报NoSuchElementException
                if (noBigEnough.size > 1) {
                    perfectSize = Collections.min(
                        noBigEnough,
                        CompareAreaSize()
                    )
                } else if (noBigEnough.size == 1) {
                    perfectSize = noBigEnough[0]
                }
            CalculateType.Max ->                 // 如果bigEnough只有一个元素，使用Collections.max就会因越界报NoSuchElementException
                // 因此，当只有一个元素时，直接使用该元素
                if (bigEnough.size > 1) {
                    perfectSize = Collections.max(
                        bigEnough,
                        CompareAreaSize()
                    )
                } else if (bigEnough.size == 1) {
                    perfectSize = bigEnough[0]
                }
            CalculateType.Lower ->                 // 优先查找比期望尺寸小一点的，否则找大一点的，接受范围在0.8左右
                if (noBigEnough.size > 0) {
                    val size = Collections.max(
                        noBigEnough,
                       CompareAreaSize()
                    )
                    if (size.width.toFloat() / expectWidth >= 0.8
                        && size.height.toFloat() / expectHeight > 0.8
                    ) {
                        perfectSize = size
                    }
                } else if (bigEnough.size > 0) {
                    val size = Collections.min(
                        bigEnough,
                        CompareAreaSize()
                    )
                    if (expectWidth.toFloat() / size.width >= 0.8
                        && (expectHeight / size.height).toFloat() >= 0.8
                    ) {
                        perfectSize = size
                    }
                }
            CalculateType.Larger ->                 // 优先查找比期望尺寸大一点的，否则找小一点的，接受范围在0.8左右
                if (bigEnough.size > 0) {
                    val size = Collections.min(
                        bigEnough,
                        CompareAreaSize()
                    )
                    if (expectWidth.toFloat() / size.width >= 0.8
                        && (expectHeight / size.height).toFloat() >= 0.8
                    ) {
                        perfectSize = size
                    }
                } else if (noBigEnough.size > 0) {
                    val size = Collections.max(
                        noBigEnough,
                        CompareAreaSize()
                    )
                    if (size.width.toFloat() / expectWidth >= 0.8
                        && size.height.toFloat() / expectHeight > 0.8
                    ) {
                        perfectSize = size
                    }
                }
        }
        // 如果经过前面的步骤没找到合适的尺寸，则计算最接近expectWidth * expectHeight的值
        if (perfectSize == null) {
            var result = sizes[0]
            var widthOrHeight = false // 判断存在宽或高相等的Size
            // 辗转计算宽高最接近的值
            for (size in sizes) {
                // 如果宽高相等，则直接返回
                if (size.width == expectWidth && size.height == expectHeight && size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio) {
                    result = size
                    break
                }
                // 仅仅是宽度相等，计算高度最接近的size
                if (size.width == expectWidth) {
                    widthOrHeight = true
                    if (Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight)
                        && size.height.toFloat() / size.width.toFloat() == CameraParam.instance.currentRatio
                    ) {
                        result = size
                        break
                    }
                } else if (size.height == expectHeight) {
                    widthOrHeight = true
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth)
                        && size.height.toFloat() / size.width.toFloat() == CameraParam.instance.currentRatio
                    ) {
                        result = size
                        break
                    }
                } else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth) && Math.abs(
                            result.height - expectHeight
                        ) > Math.abs(size.height - expectHeight) && size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio
                    ) {
                        result = size
                    }
                }
            }
            perfectSize = result
        }
        return perfectSize
    }

    /**
     * 分辨率由大到小排序
     * @param list
     */
    private fun sortList(list: List<Camera.Size>) {
        Collections.sort(list, CompareAreaSize())
    }

    /**
     * 比较器
     */
    private class CompareAreaSize : Comparator<Camera.Size> {
        override fun compare(pre: Camera.Size, after: Camera.Size): Int {
            return java.lang.Long.signum(
                pre.width.toLong() * pre.height -
                        after.width.toLong() * after.height
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun switchCamera() {
        var front: Boolean = !isFrontCamera()
        front = front && CameraApi.hasFrontCamera(activity)
        // 期望值不一致
        if (front != isFrontCamera()) {
            setFront(front)
            openCamera()
        }
    }

    /**
     * 选择合适的FPS
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    private fun chooseFixedPreviewFps(
        parameters: Camera.Parameters?,
        expectedThoudandFps: Int
    ): Int {
        val supportedFps = parameters?.supportedPreviewFpsRange
        if (supportedFps != null) {
            for (entry in supportedFps) {
                if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                    parameters.setPreviewFpsRange(entry[0], entry[1])
                    return entry[0]
                }
            }
        }
        val temp = IntArray(2)
        parameters?.getPreviewFpsRange(temp)
        return if (temp[0] == temp[1]) {
            temp[0]
        } else {
            temp[1] / 2
        }
    }

}