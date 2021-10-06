package com.myl.camerasdk.listener

/**
 * 预览回调数据
 */
interface PreviewCallback {
    fun onPreviewFrame(data: ByteArray?)
}