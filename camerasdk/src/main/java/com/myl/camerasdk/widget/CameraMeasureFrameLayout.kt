package com.myl.camerasdk.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes

class CameraMeasureFrameLayout(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 计算MeasureLayout
        if (mListener != null) {
            mListener!!.onMeasure(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
            )
        }
    }

    interface OnMeasureListener {
        fun onMeasure(width: Int, height: Int)
    }

    /**
     * 监听处理
     */
    fun setOnMeasureListener(listener: OnMeasureListener?) {
        mListener = listener
    }

    private var mListener: OnMeasureListener? = null
}