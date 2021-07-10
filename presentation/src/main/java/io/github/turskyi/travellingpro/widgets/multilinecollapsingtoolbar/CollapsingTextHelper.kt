package io.github.turskyi.travellingpro.widgets.multilinecollapsingtoolbar

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.core.math.MathUtils
import androidx.core.text.TextDirectionHeuristicsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import io.github.turskyi.travellingpro.R
import kotlin.math.abs
import kotlin.math.roundToInt

internal class CollapsingTextHelper(private val mView: View) {
    companion object {
        /* Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
         * by using our own texture */
        private const val USE_SCALING_TEXTURE: Boolean = false
        private const val DEBUG_DRAW: Boolean = false
        private var DEBUG_DRAW_PAINT: Paint? = null

        /**
         * Returns true if `value` is 'close' to it's closest decimal value. Close is currently
         * defined as it's difference being < 0.001.
         */
        private fun isClose(value: Float, targetValue: Float): Boolean {
            return abs(value - targetValue) < 0.001f
        }

        /**
         * Blend `color1` and `color2` using the given ratio.
         *
         * @param ratio of which to blend. 0.0 will return `color1`, 0.5 will give an even blend,
         * 1.0 will return `color2`.
         */
        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio: Float = 1f - ratio
            val colorAlpha: Float = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio)
            val colorRed: Float = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio)
            val colorGreen: Float = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio)
            val colorBlue: Float = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio)
            return Color.argb(colorAlpha.toInt(), colorRed.toInt(), colorGreen.toInt(), colorBlue.toInt())
        }

        private fun lerp(
            startValue: Float, endValue: Float, fraction: Float,
            interpolator: Interpolator?
        ): Float {
            var lerpFraction: Float = fraction
            if (interpolator != null) {
                lerpFraction = interpolator.getInterpolation(lerpFraction)
            }
            return AnimationUtils.lerp(startValue, endValue, lerpFraction)
        }

        private fun rectEquals(rect: Rect, left: Int, top: Int, right: Int, bottom: Int): Boolean {
            return !((rect.left != left) || (rect.top != top) || (rect.right != right) || (rect.bottom != bottom))
        }

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            if (DEBUG_DRAW_PAINT != null) {
                DEBUG_DRAW_PAINT!!.isAntiAlias = true
                DEBUG_DRAW_PAINT!!.color = Color.MAGENTA
            }
        }
    }

    private var mDrawTitle: Boolean = false
    private var mExpandedFraction: Float = 0f
    private val mExpandedBounds: Rect = Rect()
    private val mCollapsedBounds: Rect = Rect()
    private val mCurrentBounds: RectF = RectF()
    private var mExpandedTextGravity: Int = Gravity.CENTER_VERTICAL
    private var mCollapsedTextGravity: Int = Gravity.CENTER_VERTICAL
    private var mExpandedTextSize: Float = 15f
    private var mCollapsedTextSize: Float = 15f
    var expandedTextColor: ColorStateList? = null
        private set
    var collapsedTextColor: ColorStateList? = null
        private set
    private var mExpandedDrawY: Float = 0f
    private var mCollapsedDrawY: Float = 0f
    private var mExpandedDrawX: Float = 0f
    private var mCollapsedDrawX: Float = 0f
    private var mCurrentDrawX: Float = 0f
    private var mCurrentDrawY: Float = 0f
    private var mCollapsedTypeface: Typeface? = null
    private var mExpandedTypeface: Typeface? = null
    private var mCurrentTypeface: Typeface? = null
    private var mText: CharSequence? = null
    private var mTextToDraw: CharSequence? = null
    private var mIsRtl: Boolean = false
    private var mUseTexture: Boolean = false
    private var mExpandedTitleTexture: Bitmap? = null
    private var mTexturePaint: Paint? = null

    // Removed now unused fields mTextureAscent and mTextureDescent
    private var mScale: Float = 0f
    private var mCurrentTextSize: Float = 0f
    private var mState: IntArray? = null
    private var mBoundsChanged: Boolean = false
    private val mTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private var mPositionInterpolator: Interpolator? = null
    private var mTextSizeInterpolator: Interpolator? = null
    private var mCollapsedShadowRadius: Float = 0f
    private var mCollapsedShadowDx: Float = 0f
    private var mCollapsedShadowDy: Float = 0f
    private var mCollapsedShadowColor: Int = 0
    private var mExpandedShadowRadius: Float = 0f
    private var mExpandedShadowDx: Float = 0f
    private var mExpandedShadowDy: Float = 0f
    private var mExpandedShadowColor: Int = 0

    // Added fields
    private var mTextToDrawCollapsed: CharSequence? = null
    private var mCollapsedTitleTexture: Bitmap? = null
    private var mCrossSectionTitleTexture: Bitmap? = null
    private var mTextLayout: StaticLayout? = null
    private var mCollapsedTextBlend: Float = 0f
    private var mExpandedTextBlend: Float = 0f
    private var mExpandedFirstLineDrawX: Float = 0f
    private var maxLines: Int = 3
    private var lineSpacingExtra: Float = 0f
    private var lineSpacingMultiplier: Float = 1f
    fun setTextSizeInterpolator(interpolator: Interpolator?) {
        mTextSizeInterpolator = interpolator
        recalculate()
    }

    fun setPositionInterpolator(interpolator: Interpolator?) {
        mPositionInterpolator = interpolator
        recalculate()
    }

    fun setCollapsedTextColor(textColor: ColorStateList) {
        if (collapsedTextColor !== textColor) {
            collapsedTextColor = textColor
            recalculate()
        }
    }

    fun setExpandedTextColor(textColor: ColorStateList) {
        if (expandedTextColor !== textColor) {
            expandedTextColor = textColor
            recalculate()
        }
    }

    fun setExpandedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom)
            mBoundsChanged = true
            onBoundsChanged()
        }
    }

    fun setCollapsedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom)
            mBoundsChanged = true
            onBoundsChanged()
        }
    }

    private fun onBoundsChanged() {
        mDrawTitle = (mCollapsedBounds.width() > 0) && (mCollapsedBounds.height() > 0
                ) && (mExpandedBounds.width() > 0) && (mExpandedBounds.height() > 0)
    }

    var expandedTextGravity: Int
        get() = mExpandedTextGravity
        set(gravity) {
            if (mExpandedTextGravity != gravity) {
                mExpandedTextGravity = gravity
                recalculate()
            }
        }
    var collapsedTextGravity: Int
        get() = mCollapsedTextGravity
        set(gravity) {
            if (mCollapsedTextGravity != gravity) {
                mCollapsedTextGravity = gravity
                recalculate()
            }
        }

    fun setCollapsedTextAppearance(resId: Int) {
        val typedArray: TypedArray = mView.context.obtainStyledAttributes(
            resId,
            R.styleable.TextAppearance
        )
        if (typedArray.hasValue(R.styleable.TextAppearance_android_textColor)) {
            collapsedTextColor = typedArray.getColorStateList(
                R.styleable.TextAppearance_android_textColor
            )
        }
        if (typedArray.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTextSize = typedArray.getDimensionPixelSize(
                R.styleable.TextAppearance_android_textSize,
                mCollapsedTextSize.toInt()
            ).toFloat()
        }
        mCollapsedShadowColor = typedArray.getInt(
            R.styleable.TextAppearance_android_shadowColor, 0
        )
        mCollapsedShadowDx = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowDx, 0f
        )
        mCollapsedShadowDy = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowDy, 0f
        )
        mCollapsedShadowRadius = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowRadius, 0f
        )
        typedArray.recycle()
        mCollapsedTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    fun setExpandedTextAppearance(resId: Int) {
        val typedArray: TypedArray = mView.context.obtainStyledAttributes(
            resId,
            R.styleable.TextAppearance
        )
        if (typedArray.hasValue(R.styleable.TextAppearance_android_textColor)) {
            expandedTextColor = typedArray.getColorStateList(
                R.styleable.TextAppearance_android_textColor
            )
        }
        if (typedArray.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mExpandedTextSize = typedArray.getDimensionPixelSize(
                R.styleable.TextAppearance_android_textSize,
                mExpandedTextSize.toInt()
            ).toFloat()
        }
        mExpandedShadowColor = typedArray.getInt(
            R.styleable.TextAppearance_android_shadowColor, 0
        )
        mExpandedShadowDx = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowDx, 0f
        )
        mExpandedShadowDy = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowDy, 0f
        )
        mExpandedShadowRadius = typedArray.getFloat(
            R.styleable.TextAppearance_android_shadowRadius, 0f
        )
        typedArray.recycle()
        mExpandedTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    //  getter and setter method for number of max lines
    fun setMaxLines(maxLines: Int) {
        if (maxLines != this.maxLines) {
            this.maxLines = maxLines
            clearTexture()
            recalculate()
        }
    }

    fun getMaxLines(): Int {
        return maxLines
    }

    // getter and setter methods for line spacing
    fun setLineSpacingExtra(lineSpacingExtra: Float) {
        if (lineSpacingExtra != this.lineSpacingExtra) {
            this.lineSpacingExtra = lineSpacingExtra
            clearTexture()
            recalculate()
        }
    }

    fun getLineSpacingExtra(): Float {
        return lineSpacingExtra
    }

    fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        if (lineSpacingMultiplier != this.lineSpacingMultiplier) {
            this.lineSpacingMultiplier = lineSpacingMultiplier
            clearTexture()
            recalculate()
        }
    }

    fun getLineSpacingMultiplier(): Float {
        return lineSpacingMultiplier
    }

    private fun readFontFamilyTypeface(resId: Int): Typeface? {
        val typedArray: TypedArray =
            mView.context.obtainStyledAttributes(resId, intArrayOf(R.attr.fontFamily))
        try {
            val family: String? = typedArray.getString(0)
            if (family != null) {
                return Typeface.create(family, Typeface.NORMAL)
            }
        } finally {
            typedArray.recycle()
        }
        return null
    }

    fun setTypefaces(typeface: Typeface?) {
        mExpandedTypeface = typeface
        mCollapsedTypeface = mExpandedTypeface
        recalculate()
    }

    var collapsedTypeface: Typeface?
        get() = if (mCollapsedTypeface != null) mCollapsedTypeface else Typeface.DEFAULT
        set(typeface) {
            if (areTypefacesDifferent(mCollapsedTypeface, typeface)) {
                mCollapsedTypeface = typeface
                recalculate()
            }
        }
    var expandedTypeface: Typeface?
        get() = if (mExpandedTypeface != null) mExpandedTypeface else Typeface.DEFAULT
        set(typeface) {
            if (areTypefacesDifferent(mExpandedTypeface, typeface)) {
                mExpandedTypeface = typeface
                recalculate()
            }
        }

    fun setState(state: IntArray?): Boolean {
        mState = state
        if (isStateful) {
            recalculate()
            return true
        }
        return false
    }

    private val isStateful: Boolean
        get() = (collapsedTextColor != null && collapsedTextColor!!.isStateful) ||
                (expandedTextColor != null && expandedTextColor!!.isStateful)

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     *
     * A value of `0.0` indicates that the layout is fully expanded.
     * A value of `1.0` indicates that the layout is fully collapsed.
     */
    var expansionFraction: Float
        get() {
            return mExpandedFraction
        }
        set(fraction) {
            var mFraction: Float = fraction
            mFraction = MathUtils.clamp(mFraction, 0f, 1f)
            if (mFraction != mExpandedFraction) {
                mExpandedFraction = mFraction
                calculateCurrentOffsets()
            }
        }
    var collapsedTextSize: Float
        get() {
            return mCollapsedTextSize
        }
        set(textSize) {
            if (mCollapsedTextSize != textSize) {
                mCollapsedTextSize = textSize
                recalculate()
            }
        }
    var expandedTextSize: Float
        get() {
            return mExpandedTextSize
        }
        set(textSize) {
            if (mExpandedTextSize != textSize) {
                mExpandedTextSize = textSize
                recalculate()
            }
        }

    private fun calculateCurrentOffsets() {
        calculateOffsets(mExpandedFraction)
    }

    private fun calculateOffsets(fraction: Float) {
        interpolateBounds(fraction)
        mCurrentDrawX = lerp(
            mExpandedDrawX, mCollapsedDrawX, fraction,
            mPositionInterpolator
        )
        mCurrentDrawY = lerp(
            mExpandedDrawY, mCollapsedDrawY, fraction,
            mPositionInterpolator
        )
        setInterpolatedTextSize(
            lerp(
                mExpandedTextSize, mCollapsedTextSize,
                fraction, mTextSizeInterpolator
            )
        )

        //  set text blending
        setCollapsedTextBlend(
            1 - lerp(
                0f,
                1f,
                1 - fraction,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        )
        setExpandedTextBlend(lerp(1f, 0f, fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR))

        if (collapsedTextColor !== expandedTextColor) {
            /* If the collapsed and expanded text colors are different, blend them based on the
             * fraction */
            mTextPaint.color = blendColors(
                currentExpandedTextColor, currentCollapsedTextColor, fraction
            )
        } else {
            mTextPaint.color = currentCollapsedTextColor
        }
        mTextPaint.setShadowLayer(
            lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
            lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
            lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
            blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction)
        )
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    @get:ColorInt
    private val currentExpandedTextColor: Int
        get() {
            return if (mState != null) {
                expandedTextColor!!.getColorForState(mState, 0)
            } else {
                expandedTextColor!!.defaultColor
            }
        }

    @get:ColorInt
    private val currentCollapsedTextColor: Int
        get() {
            return if (mState != null) {
                collapsedTextColor!!.getColorForState(mState, 0)
            } else {
                collapsedTextColor!!.defaultColor
            }
        }

    private fun calculateBaseOffsets() {
        val currentTextSize: Float = mCurrentTextSize
        // We then calculate the collapsed text size, using the same logic
        calculateUsingTextSize(mCollapsedTextSize)

        // set mTextToDrawCollapsed and calculate width using it
        mTextToDrawCollapsed = mTextToDraw
        var width: Float = if (mTextToDrawCollapsed != null) mTextPaint.measureText(
            mTextToDrawCollapsed,
            0,
            mTextToDrawCollapsed!!.length
        ) else 0F

        val collapsedAbsGravity: Int = GravityCompat.getAbsoluteGravity(
            mCollapsedTextGravity,
            if (mIsRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        )

        //  calculate height and Y position using mTextLayout
        var textHeight: Float =
            if (mTextLayout != null) mTextLayout!!.height.toFloat() else 0.toFloat()
        mCollapsedDrawY = when (collapsedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> mCollapsedBounds.bottom - textHeight
            Gravity.TOP -> mCollapsedBounds.top.toFloat()
            Gravity.CENTER_VERTICAL -> {
                val textOffset: Float = (textHeight / 2)
                mCollapsedBounds.centerY() - textOffset
            }
            else -> {
                val textOffset: Float = (textHeight / 2)
                mCollapsedBounds.centerY() - textOffset
            }
        }
        mCollapsedDrawX = when (collapsedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> mCollapsedBounds.centerX() - (width / 2)
            Gravity.END -> mCollapsedBounds.right - width
            Gravity.START -> mCollapsedBounds.left.toFloat()
            else -> mCollapsedBounds.left.toFloat()
        }
        calculateUsingTextSize(mExpandedTextSize)

        // calculate width using mTextLayout based on first line and store that padding
        width = if (mTextLayout != null) mTextLayout!!.getLineWidth(0) else 0F
        mExpandedFirstLineDrawX = if (mTextLayout != null) mTextLayout!!.getLineLeft(0) else 0F

        val expandedAbsGravity: Int = GravityCompat.getAbsoluteGravity(
            mExpandedTextGravity,
            if (mIsRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        )

        //  calculate height and Y position using mTextLayout
        textHeight = if (mTextLayout != null) mTextLayout!!.height.toFloat() else 0.toFloat()
        mExpandedDrawY = when (expandedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> mExpandedBounds.bottom - textHeight
            Gravity.TOP -> mExpandedBounds.top.toFloat()
            Gravity.CENTER_VERTICAL -> {
                val textOffset: Float = (textHeight / 2)
                mExpandedBounds.centerY() - textOffset
            }
            else -> {
                val textOffset: Float = (textHeight / 2)
                mExpandedBounds.centerY() - textOffset
            }
        }
        mExpandedDrawX = when (expandedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> mExpandedBounds.centerX() - (width / 2)
            Gravity.END -> mExpandedBounds.right - width
            Gravity.START -> mExpandedBounds.left.toFloat()
            else -> mExpandedBounds.left.toFloat()
        }

        // The bounds have changed so we need to clear the texture
        clearTexture()
        // Now reset the text size back to the original
        setInterpolatedTextSize(currentTextSize)
    }

    private fun interpolateBounds(fraction: Float) {
        mCurrentBounds.left = lerp(
            mExpandedBounds.left.toFloat(), mCollapsedBounds.left.toFloat(),
            fraction, mPositionInterpolator
        )
        mCurrentBounds.top = lerp(
            mExpandedDrawY, mCollapsedDrawY,
            fraction, mPositionInterpolator
        )
        mCurrentBounds.right = lerp(
            mExpandedBounds.right.toFloat(), mCollapsedBounds.right.toFloat(),
            fraction, mPositionInterpolator
        )
        mCurrentBounds.bottom = lerp(
            mExpandedBounds.bottom.toFloat(), mCollapsedBounds.bottom.toFloat(),
            fraction, mPositionInterpolator
        )
    }

    fun draw(canvas: Canvas) {
        val saveCount: Int = canvas.save()
        if (mTextToDraw != null && mDrawTitle) {
            val x: Float = mCurrentDrawX
            val y: Float = mCurrentDrawY
            val drawTexture: Boolean = mUseTexture && mExpandedTitleTexture != null
            // removed now unused "descent" variable declaration

            // Update the TextPaint to the current text size
            mTextPaint.textSize = mCurrentTextSize

            //  new drawing code
            val ascent: Float = if (drawTexture) {
                0f
            } else {
                mTextPaint.ascent() * mScale
            }
            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a magenta rect in the text bounds
                canvas.drawRect(
                    mCurrentBounds.left, y, mCurrentBounds.right,
                    y + mTextLayout!!.height * mScale,
                    (DEBUG_DRAW_PAINT)!!
                )
            }
            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y)
            }

            // Compute where to draw mTextLayout for this frame
            val currentExpandedX: Float =
                mCurrentDrawX + mTextLayout!!.getLineLeft(0) - mExpandedFirstLineDrawX * 2
            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                // Expanded text
                mTexturePaint!!.alpha = (mExpandedTextBlend * 255).toInt()
                canvas.drawBitmap((mExpandedTitleTexture)!!, currentExpandedX, y, mTexturePaint)
                // Collapsed text
                mTexturePaint!!.alpha = (mCollapsedTextBlend * 255).toInt()
                canvas.drawBitmap((mCollapsedTitleTexture)!!, x, y, mTexturePaint)
                // Cross-section between both texts (should stay at alpha = 255)
                mTexturePaint!!.alpha = 255
                canvas.drawBitmap((mCrossSectionTitleTexture)!!, x, y, mTexturePaint)
            } else {
                // position expanded text appropriately
                canvas.translate(currentExpandedX, y)
                // Expanded text
                mTextPaint.alpha = (mExpandedTextBlend * 255).toInt()
                mTextLayout!!.draw(canvas)

                // position the overlays
                canvas.translate(x - currentExpandedX, 0f)

                // Collapsed text
                mTextPaint.alpha = (mCollapsedTextBlend * 255).toInt()
                canvas.drawText(
                    (mTextToDrawCollapsed)!!, 0, mTextToDrawCollapsed!!.length, 0f,
                    -ascent / mScale, mTextPaint
                )
                // Remove ellipsis for Cross-section animation
                var tmp: String = mTextToDrawCollapsed.toString().trim { it <= ' ' }
                if (tmp.endsWith("\u2026")) {
                    tmp = tmp.substring(0, tmp.length - 1)
                }
                // Cross-section between both texts (should stay at alpha = 255)
                mTextPaint.alpha = 255
                canvas.drawText(
                    tmp,
                    0,
                    if (mTextLayout!!.getLineEnd(0) <= tmp.length) mTextLayout!!.getLineEnd(0) else tmp.length,
                    0f,
                    -ascent / mScale,
                    mTextPaint
                )
            }
        }
        canvas.restoreToCount(saveCount)
    }

    private fun calculateIsRtl(text: CharSequence?): Boolean {
        val defaultIsRtl: Boolean = (ViewCompat.getLayoutDirection(mView)
                == ViewCompat.LAYOUT_DIRECTION_RTL)
        return (if (defaultIsRtl) TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL else TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(
            text,
            0,
            text!!.length
        )
    }

    private fun setInterpolatedTextSize(textSize: Float) {
        calculateUsingTextSize(textSize)
        // Use our texture if the scale isn't 1.0
        mUseTexture = USE_SCALING_TEXTURE && mScale != 1f
        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture()
            // added collapsed and cross section textures
            ensureCollapsedTexture()
            ensureCrossSectionTexture()
        }
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    // new setCollapsedTextBlend and setExpandedTextBlend methods
    private fun setCollapsedTextBlend(blend: Float) {
        mCollapsedTextBlend = blend
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    private fun setExpandedTextBlend(blend: Float) {
        mExpandedTextBlend = blend
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    private fun areTypefacesDifferent(first: Typeface?, second: Typeface?): Boolean {
        return (first != null && first != second) || (first == null && second != null)
    }

    private fun calculateUsingTextSize(textSize: Float) {
        if (mText == null) return
        val collapsedWidth: Float = mCollapsedBounds.width().toFloat()
        val expandedWidth: Float = mExpandedBounds.width().toFloat()
        val availableWidth: Float
        val newTextSize: Float
        var updateDrawText = false
        // Add maxLines variable
        val maxLines: Int
        if (isClose(textSize, mCollapsedTextSize)) {
            newTextSize = mCollapsedTextSize
            mScale = 1f
            if (areTypefacesDifferent(mCurrentTypeface, mCollapsedTypeface)) {
                mCurrentTypeface = mCollapsedTypeface
                updateDrawText = true
            }
            availableWidth = collapsedWidth
            //Set maxLines variable
            maxLines = 1
        } else {
            newTextSize = mExpandedTextSize
            if (areTypefacesDifferent(mCurrentTypeface, mExpandedTypeface)) {
                mCurrentTypeface = mExpandedTypeface
                updateDrawText = true
            }
            mScale = if (isClose(textSize, mExpandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                1f
            } else {
                // Else, we'll scale down from the expanded text size
                textSize / mExpandedTextSize
            }
            val textSizeRatio: Float = mCollapsedTextSize / mExpandedTextSize
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed text size
            val scaledDownWidth: Float = expandedWidth * textSizeRatio
            availableWidth = if (scaledDownWidth > collapsedWidth) {
                /* If the scaled down size is larger than the actual collapsed width, we need to
                  cap the available width so that when the expanded text scales down, it matches
                  the collapsed width */

                expandedWidth
            } else {
                // Otherwise we'll just use the expanded width
                expandedWidth
            }

            //  Set maxLines variable
            maxLines = this.maxLines
        }
        if (availableWidth > 0) {
            updateDrawText = (mCurrentTextSize != newTextSize) || mBoundsChanged || updateDrawText
            mCurrentTextSize = newTextSize
            mBoundsChanged = false
        }
        if (mTextToDraw == null || updateDrawText) {
            mTextPaint.textSize = mCurrentTextSize
            mTextPaint.typeface = mCurrentTypeface

            //  Text layout creation and text truncation
            val layout = StaticLayout(
                mText, mTextPaint,
                availableWidth.toInt(),
                Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, false
            )
            val truncatedText: CharSequence
            if (layout.lineCount > maxLines) {
                val lastLine: Int = maxLines - 1
                val textBefore: CharSequence = if (lastLine > 0) mText!!.subSequence(
                    0,
                    layout.getLineEnd(lastLine - 1)
                ) else ""
                var lineText: CharSequence = mText!!.subSequence(
                    layout.getLineStart(lastLine),
                    layout.getLineEnd(lastLine)
                )
                // if last char in line is space, move it behind the ellipsis
                var lineEnd: CharSequence? = ""
                if (lineText.get(lineText.length - 1) == ' ') {
                    lineEnd = lineText.subSequence(lineText.length - 1, lineText.length)
                    lineText = lineText.subSequence(0, lineText.length - 1)
                }
                // insert ellipsis character
                lineText = TextUtils.concat(lineText, "\u2026", lineEnd)
                // if the text is too long, truncate it
                val truncatedLineText: CharSequence = TextUtils.ellipsize(
                    lineText, mTextPaint,
                    availableWidth, TextUtils.TruncateAt.END
                )
                truncatedText = TextUtils.concat(textBefore, truncatedLineText)
            } else {
                truncatedText = mText as CharSequence
            }
            if (!TextUtils.equals(truncatedText, mTextToDraw)) {
                mTextToDraw = truncatedText
                mIsRtl = calculateIsRtl(mTextToDraw)
            }
            val alignment: Layout.Alignment = when (mExpandedTextGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER
                Gravity.END-> Layout.Alignment.ALIGN_OPPOSITE
                 Gravity.START -> Layout.Alignment.ALIGN_NORMAL
                else -> Layout.Alignment.ALIGN_NORMAL
            }
            mTextLayout = StaticLayout(
                mTextToDraw, mTextPaint, availableWidth.toInt(),
                alignment, lineSpacingMultiplier, lineSpacingExtra, false
            )
        }
    }

    private fun ensureExpandedTexture() {
        if (((mExpandedTitleTexture != null) || mExpandedBounds.isEmpty
                    || TextUtils.isEmpty(mTextToDraw))
        ) {
            return
        }
        calculateOffsets(0f)

        // Calculate width and height using mTextLayout and remove
        // mTextureAscent and mTextureDescent assignment
        val w: Int = mTextLayout!!.width
        val h: Int = mTextLayout!!.height

        if (w <= 0 || h <= 0) {
            return  // If the width or height are 0, return
        }
        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        //  Draw text using mTextLayout
        val canvas = Canvas(mExpandedTitleTexture as Bitmap)
        mTextLayout!!.draw(canvas)
        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        }
    }

    // new ensureCollapsedTexture and ensureCrossSectionTexture methods
    private fun ensureCollapsedTexture() {
        if (((mCollapsedTitleTexture != null) || mCollapsedBounds.isEmpty
                    || TextUtils.isEmpty(mTextToDraw))
        ) {
            return
        }
        calculateOffsets(0f)
        val w: Int = mTextPaint.measureText(mTextToDraw, 0, mTextToDraw!!.length).roundToInt()
        val h: Int = (mTextPaint.descent() - mTextPaint.ascent()).roundToInt()
        if (w <= 0 && h <= 0) {
            return  // If the width or height are 0, return
        }
        mCollapsedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mCollapsedTitleTexture as Bitmap)
        canvas.drawText(
            (mTextToDrawCollapsed)!!, 0, mTextToDrawCollapsed!!.length, 0f,
            -mTextPaint.ascent() / mScale, mTextPaint
        )
        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        }
    }

    private fun ensureCrossSectionTexture() {
        if (((mCrossSectionTitleTexture != null) || mCollapsedBounds.isEmpty
                    || TextUtils.isEmpty(mTextToDraw))
        ) {
            return
        }
        calculateOffsets(0f)
        val w: Int = mTextPaint.measureText(
            mTextToDraw, mTextLayout!!.getLineStart(0),
            mTextLayout!!.getLineEnd(0)
        ).roundToInt()
        val h: Int = (mTextPaint.descent() - mTextPaint.ascent()).roundToInt()
        if (w <= 0 && h <= 0) {
            return  // If the width or height are 0, return
        }
        mCrossSectionTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mCrossSectionTitleTexture as Bitmap)
        var tmp: String = mTextToDrawCollapsed.toString().trim { it <= ' ' }
        if (tmp.endsWith("\u2026")) {
            tmp = tmp.substring(0, tmp.length - 1)
        }
        canvas.drawText(
            tmp,
            0,
            if (mTextLayout!!.getLineEnd(0) <= tmp.length) mTextLayout!!.getLineEnd(0) else tmp.length,
            0f,
            -mTextPaint.ascent() / mScale,
            mTextPaint
        )
        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        }
    }

    fun recalculate() {
        if (mView.height > 0 && mView.width > 0) {
            /* If we've already been laid out, calculate everything now otherwise we'll wait
             * until a layout */
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    /**
     * Set the title to display
     */
    var text: CharSequence?
        get() {
            return mText
        }
        set(text) {
            if (text == null || text != mText) {
                mText = text
                mTextToDraw = null
                clearTexture()
                recalculate()
            }
        }

    private fun clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture!!.recycle()
            mExpandedTitleTexture = null
        }
        //  clear other textures
        if (mCollapsedTitleTexture != null) {
            mCollapsedTitleTexture!!.recycle()
            mCollapsedTitleTexture = null
        }
        if (mCrossSectionTitleTexture != null) {
            mCrossSectionTitleTexture!!.recycle()
            mCrossSectionTitleTexture = null
        }
    }

}
