package io.github.turskyi.travellingpro.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView

class ListenableWebView : WebView, View.OnTouchListener {
    companion object {
        private const val FINGER_RELEASED: Int = 0
        private const val FINGER_TOUCHED: Int = 1
        private const val FINGER_DRAGGING: Int = 2
        private const val FINGER_UNDEFINED: Int = 3
    }

    private var fingerState: Int = FINGER_RELEASED

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                fingerState = if (fingerState == FINGER_RELEASED) {
                    FINGER_TOUCHED
                } else {
                    FINGER_UNDEFINED
                }
            }
            MotionEvent.ACTION_UP -> when {
                fingerState != FINGER_DRAGGING -> {
                    fingerState = FINGER_RELEASED
                    /* For this particular app we want the main work to happen
                     * on ACTION_UP rather than ACTION_DOWN. So this is where
                     * we will call performClick(). */
                    performClick()
                }
                else -> fingerState = if (fingerState == FINGER_DRAGGING) {
                    FINGER_RELEASED
                } else {
                    FINGER_UNDEFINED
                }
            }
            else -> fingerState = if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                if (fingerState == FINGER_TOUCHED || fingerState == FINGER_DRAGGING) {
                    FINGER_DRAGGING
                } else {
                    FINGER_UNDEFINED
                }
            } else {
                FINGER_UNDEFINED
            }
        }
        return false
    }

    /* Because we call this from onTouchEvent, this code will be executed for both
     * normal touch events and for when the system calls this using Accessibility */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}