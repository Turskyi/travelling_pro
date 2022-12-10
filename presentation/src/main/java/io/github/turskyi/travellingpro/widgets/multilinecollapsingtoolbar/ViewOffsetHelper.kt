package io.github.turskyi.travellingpro.widgets.multilinecollapsingtoolbar

import android.view.View
import androidx.core.view.ViewCompat

/**
 * Utility helper for moving a [android.view.View] around using
 * [android.view.View.offsetLeftAndRight] and
 * [android.view.View.offsetTopAndBottom].
 *
 *
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
class ViewOffsetHelper(private val mView: View) {
    var layoutTop = 0
        private set
    private var layoutLeft = 0
    private var topAndBottomOffset = 0
    private var leftAndRightOffset = 0

    fun onViewLayout() {
        // Now grab the intended top
        layoutTop = mView.top
        layoutLeft = mView.left

        // And offset it as needed
        updateOffsets()
    }

    private fun updateOffsets() {
        ViewCompat.offsetTopAndBottom(mView, topAndBottomOffset - (mView.top - layoutTop))
        ViewCompat.offsetLeftAndRight(mView, leftAndRightOffset - (mView.left - layoutLeft))
    }

    /**
     * Set the top and bottom offset for this [ViewOffsetHelper]'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    @Suppress("BooleanMethodIsAlwaysInverted")
    fun setTopAndBottomOffset(offset: Int): Boolean {
        if (topAndBottomOffset != offset) {
            topAndBottomOffset = offset
            updateOffsets()
            return true
        }
        return false
    }
}