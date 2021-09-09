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
import android.view.View
import android.widget.LinearLayout
import io.github.turskyi.travellingpro.R

/**
 * layout with colored shadow
 * */
class ShadowedLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var shadowColor: Int
    private var containerBackgroundColor: Int
    private var shadowRadius: Float
    private var containerElevation: Float
    private var shadowGravity: String?
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
        if(containerElevation != to) {
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

    init {
        /* populate values from xml attributes */
        val array = context.obtainStyledAttributes(attrs, R.styleable.ShadowedLinearLayout)
        containerBackgroundColor =
            array.getColor(R.styleable.ShadowedLinearLayout_containerBackgroundColor, Color.WHITE)
        shadowRadius = array.getDimension(R.styleable.ShadowedLinearLayout_shadowRadius, 12f)
        shadowColor = array.getColor(R.styleable.ShadowedLinearLayout_shadowColor, Color.BLACK)
        containerElevation =
            array.getDimension(R.styleable.ShadowedLinearLayout_containerElevation, 12f)
        shadowGravity = array.getString(R.styleable.ShadowedLinearLayout_shadowGravity)
        shadowMarginStart =
            array.getDimension(R.styleable.ShadowedLinearLayout_shadowMarginStart, 0f)
        shadowMarginTop = array.getDimension(R.styleable.ShadowedLinearLayout_shadowMarginTop, 0f)
        shadowMarginEnd = array.getDimension(R.styleable.ShadowedLinearLayout_shadowMarginEnd, 0f)
        shadowMarginBottom =
            array.getDimension(R.styleable.ShadowedLinearLayout_shadowMarginBottom, 0f)
        scaleViewOnElevationChange =
            array.getBoolean(R.styleable.ShadowedLinearLayout_scaleViewOnElevationChange, true)

        background =
            generateBackgroundWithShadow(
                containerBackgroundColor,
                shadowRadius,
                shadowColor,
                containerElevation,
                shadowGravity
            )

        array.recycle()
    }

    /**
     * @return drawable
     * @param backgroundColor drawable background color
     * drawable contains shadow with
     * @param shadowColor  shadow color
     * @param shadowRadius  radius of shadow.
     * @param gravity shadow will be visible from view side
     * @param elevation describes shadow size*/
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

        val outerRadius = floatArrayOf(
            0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f
        )

        val shapeDrawablePadding = Rect()

        shapeDrawablePadding.right = (makeHorizontalOffset(elevation) + shadowMarginEnd).toInt()
        shapeDrawablePadding.left = (makeHorizontalOffset(elevation) + shadowMarginStart).toInt()

        val shadowOffset: Float = when (shadowGravity) {
            Gravity.CENTER -> 0f
            Gravity.TOP -> -1 * elevationValue / 3f
            Gravity.BOTTOM -> elevationValue / 1.5f
            else -> elevationValue / 1.5f
        }
        shapeDrawablePadding.top = (elevationValue * 2 + shadowMarginTop).toInt()
        shapeDrawablePadding.bottom = (elevationValue * 2 + shadowMarginBottom).toInt()
        val shapeDrawable = ShapeDrawable()

        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColor

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            setLayerType(View.LAYER_TYPE_HARDWARE, shapeDrawable.paint)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        }

        if (elevation > resources.getDimension(R.dimen.elevation_1) * 2) {
            shapeDrawable.paint.setShadowLayer(shadowRadius, elevation / 5, shadowOffset, shadowColor)
        }
        y = (-(elevationValue * 2)).toFloat()
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
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