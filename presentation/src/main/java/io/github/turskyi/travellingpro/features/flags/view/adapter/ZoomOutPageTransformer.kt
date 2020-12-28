package io.github.turskyi.travellingpro.features.flags.view.adapter

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f
class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                /*     This page is way off-screen to the left. */
                position < -1 -> alpha = 0f
                position <= 1 -> { // [-1,1]
                    /* Modify the default slide transition to shrink the page as well */
                    val scaleFactor = MIN_SCALE.coerceAtLeast(1 - abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }
                    /*     Scale the page down (between MIN_SCALE and 1) */
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    /*     Fade the page relative to its size. */
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                /* This page is way off-screen to the right. */
                else -> alpha = 0f
            }
        }
    }
}