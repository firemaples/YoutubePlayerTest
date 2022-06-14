package com.firemaples.iframeyoutubeplayer.player.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout

open class YoutubeViewWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            val sixteenNineHeight = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) * 9 / 16,
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, sixteenNineHeight)
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
