package io.github.turskyi.travellingpro.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import io.github.turskyi.travellingpro.R

/**
 * layout with coloured shadow
 * */
class ShadowedLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var shadowColor: Int = Color.BLACK
    private var containerBackgroundColor: Int = Color.WHITE
    private var containerCornerRadius: Float = 0f
    private var shadowRadius: Float = 12f
    private var containerElevation: Float = 12f
    private var shadowGravity: String? = null
    private var shadowMarginTop: Float = 0f
    private var shadowMarginBottom: Float = 0f
    private var shadowMarginStart: Float = 0f
    private var shadowMarginEnd: Float = 0f
    private var scaleViewOnElevationChange = true

    /**
     * changes elevation of view
     * @param from start value
     * @param to end value with
     * @param duration in milliseconds*/
    fun elevate(from: Float, to: Float, duration: Long) {
        if (containerElevation != to) {
            containerElevation = to
            ValueAnimator.ofFloat(from, to).apply {
                addUpdateListener {
                    val radius = if (from > to) {
                        shadowRadius - shadowRadius * currentPlayTime / duration
                    } else {
                        shadowRadius * currentPlayTime / duration
                    }
                    background = generateBackgroundWithShadow(
                        containerBackgroundColor,
                        radius,
                        shadowColor,
                        animatedValue as Float,
                        shadowGravity
                    )
                    requestLayout()
                }
                this.duration = duration
                startDelay = 300
            }.start()
        }
    }

    fun setContainerBackgroundColor(color: Int) {
        containerBackgroundColor = color
        background = generateBackgroundWithShadow(
            containerBackgroundColor,
            shadowRadius,
            shadowColor,
            containerElevation,
            shadowGravity
        )
    }

    init {
        /* populate values from XML attributes */
        context.withStyledAttributes(
            attrs,
            R.styleable.ShadowedLinearLayout,
        ) {
            containerBackgroundColor =
                getColor(
                    R.styleable.ShadowedLinearLayout_containerBackgroundColor,
                    containerBackgroundColor
                )
            containerCornerRadius =
                getDimension(
                    R.styleable.ShadowedLinearLayout_containerCornerRadius,
                    containerCornerRadius
                )
            shadowRadius =
                getDimension(
                    R.styleable.ShadowedLinearLayout_shadowRadius,
                    shadowRadius
                )
            shadowColor = getColor(
                R.styleable.ShadowedLinearLayout_shadowColor,
                shadowColor
            )
            containerElevation =
                getDimension(
                    R.styleable.ShadowedLinearLayout_containerElevation,
                    containerElevation
                )
            shadowGravity =
                getString(
                    R.styleable.ShadowedLinearLayout_shadowGravity,
                )
            shadowMarginStart =
                getDimension(
                    R.styleable.ShadowedLinearLayout_shadowMarginStart,
                    shadowMarginStart
                )
            shadowMarginTop =
                getDimension(
                    R.styleable.ShadowedLinearLayout_shadowMarginTop,
                    shadowMarginTop
                )
            shadowMarginEnd =
                getDimension(
                    R.styleable.ShadowedLinearLayout_shadowMarginEnd,
                    shadowMarginEnd
                )
            shadowMarginBottom =
                getDimension(
                    R.styleable.ShadowedLinearLayout_shadowMarginBottom,
                    shadowMarginBottom
                )
            scaleViewOnElevationChange =
                getBoolean(
                    R.styleable.ShadowedLinearLayout_scaleViewOnElevationChange,
                    scaleViewOnElevationChange
                )
        }

        background =
            generateBackgroundWithShadow(
                containerBackgroundColor,
                shadowRadius,
                shadowColor,
                containerElevation,
                shadowGravity
            )
    }

    /**
     * @return drawable
     * @param backgroundColor drawable background colour
     * drawable contains shadow with
     * @param shadowColor  shadow colour
     * @param shadowRadius  radius of shadow.
     * @param gravity shadow will be visible from view side
     * @param elevation describes shadow size*/
    @Suppress("KotlinConstantConditions")
    private fun generateBackgroundWithShadow(
        backgroundColor: Int,
        shadowRadius: Float = 1f,
        shadowColor: Int,
        elevation: Float,
        gravity: String?
    ): Drawable {
        val shadowGravity = when (gravity) {
            "bottom" -> Gravity.BOTTOM
            "top" -> Gravity.TOP
            else -> Gravity.BOTTOM
        }

        val elevationValue = elevation.toInt()

        val outerRadius = FloatArray(8) { containerCornerRadius }

        val shapeDrawablePadding = Rect()

        shapeDrawablePadding.right =
            (makeHorizontalOffset(elevation) + shadowMarginEnd).toInt()
        shapeDrawablePadding.left =
            (makeHorizontalOffset(elevation) + shadowMarginStart).toInt()

        val shadowOffset: Float = when (shadowGravity) {
            Gravity.CENTER -> 0f
            Gravity.TOP -> -1 * elevationValue / 3f
            Gravity.BOTTOM -> elevationValue / 1.5f
            else -> elevationValue / 1.5f
        }
        shapeDrawablePadding.top =
            (elevationValue * 2 + shadowMarginTop).toInt()
        shapeDrawablePadding.bottom =
            (elevationValue * 2 + shadowMarginBottom).toInt()
        val shapeDrawable = ShapeDrawable()

        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColor

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }

        if (elevation > resources.getDimension(R.dimen.elevation_1) * 2) {
            shapeDrawable.paint.setShadowLayer(
                shadowRadius,
                elevation / 5,
                shadowOffset,
                shadowColor
            )
        }
        shapeDrawable.shape = RoundRectShape(
            outerRadius,
            null,
            null,
        )
        val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            (makeHorizontalOffset(elevation) + shadowMarginStart).toInt(),
            (elevationValue * 2 + shadowMarginTop).toInt(),
            (makeHorizontalOffset(elevation) + shadowMarginEnd).toInt(),
            (elevationValue * 2 + shadowMarginBottom).toInt()
        )
        return drawable
    }

    private fun makeHorizontalOffset(elevation: Float): Float {
        return if (scaleViewOnElevationChange) elevation / 2 else 0f
    }
}