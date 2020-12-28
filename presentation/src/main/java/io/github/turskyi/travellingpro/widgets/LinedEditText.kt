package io.github.turskyi.travellingpro.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import io.github.turskyi.travellingpro.R

open class LinedEditText(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {
    private val mPaint: Paint = Paint()

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = -0x80000000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mPaint.color =
            context.getColor(R.color.colorLightGrey)
    }

    override fun onDraw(canvas: Canvas) {
        val nextCount = lineCount
        val right = right
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val height = height
        val lineHeight = lineHeight
        val startCount = (height - paddingTop - paddingBottom) / lineHeight

        for (firstCount in 0 until startCount) {
            val baseline = lineHeight * (firstCount + 1) + paddingTop
            canvas.drawLine(
                paddingLeft.toFloat(),
                baseline.toFloat(), (right - paddingRight).toFloat(), baseline.toFloat(), mPaint
            )
        }

        for (secondCount in startCount until nextCount) {
            val baseline = lineHeight * (secondCount + 1) + paddingTop
            canvas.drawLine(
                paddingLeft.toFloat(),
                baseline.toFloat(), (right - paddingRight).toFloat(), baseline.toFloat(), mPaint
            )
        }
        super.onDraw(canvas)
    }
}