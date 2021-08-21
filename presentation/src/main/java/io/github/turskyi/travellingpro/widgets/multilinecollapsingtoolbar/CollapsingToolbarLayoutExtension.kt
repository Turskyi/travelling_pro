package io.github.turskyi.travellingpro.widgets.multilinecollapsingtoolbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.*
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.ViewGroupUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import androidx.core.util.ObjectsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.widgets.multilinecollapsingtoolbar.CollapsingToolbarLayoutExtension.LayoutParams
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * CollapsingToolbarLayoutExtension is a wrapper for [Toolbar] which implements a collapsing app bar.
 * It is designed to be used as a direct child of a [AppBarLayout].
 * CollapsingToolbarLayoutExtension contains the following features:
 *
 * <h4>Collapsing title</h4>
 * A title which is larger when the layout is fully visible but collapses and becomes smaller as
 * the layout is scrolled off screen. You can set the title to display via
 * [.setTitle]. The title appearance can be tweaked via the
 * `collapsedTextAppearance` and `expandedTextAppearance` attributes.
 *
 * <h4>Content scrim</h4>
 * A full-bleed scrim which is show or hidden when the scroll position has hit a certain threshold.
 * You can change this via [.setContentScrim].
 *
 * <h4>Status bar scrim</h4>
 * A scrim which is show or hidden behind the status bar when the scroll position has hit a certain
 * threshold. You can change this via [.setStatusBarScrim]. This only works
 * on [LOLLIPOP][android.os.Build.VERSION_CODES.LOLLIPOP] devices when we set to fit system
 * windows.
 *
 * <h4>Parallax scrolling children</h4>
 * Child views can opt to be scrolled within this layout in a parallax fashion.
 * See [LayoutParams.COLLAPSE_MODE_PARALLAX]
 *
 * <h4>Pinned position children</h4>
 * Child views can opt to be pinned in space globally. This is useful when implementing a
 * collapsing as it allows the [Toolbar] to be fixed in place even though this layout is
 * moving. See [LayoutParams.COLLAPSE_MODE_PIN].
 *
 *
 * **Do not manually add views to the Toolbar at run time**.
 * We will add a 'dummy view' to the Toolbar which allows us to work out the available space
 * for the title. This can interfere with any views which you add.
 *
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_contentScrim
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_statusBarScrim
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_toolbarId
 */
@SuppressLint("CustomViewStyleable")
class CollapsingToolbarLayoutExtension @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mRefreshToolbar = true
    private val mToolbarId: Int
    private var mToolbar: Toolbar? = null
    private var mToolbarDirectChild: View? = null
    private var mDummyView: View? = null
    private var mExpandedMarginStart: Int
    private var mExpandedMarginTop: Int
    private var mExpandedMarginEnd: Int
    private var mExpandedMarginBottom: Int
    private val mTmpRect = Rect()
    private val mCollapsingTextHelper: CollapsingTextHelper?
    private var mCollapsingTitleEnabled: Boolean
    private var mDrawCollapsingTitle = false
    private var mContentScrim: Drawable? = null
    var mStatusBarScrim: Drawable? = null
    private var mScrimAlpha = 0
    private var mScrimsAreShown = false
    private var mScrimAnimator: ValueAnimator? = null
    /**
     * Returns the duration in milliseconds used for scrim visibility animations.
     */
    /**
     * Set the duration used for scrim visibility animations.
     *
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_scrimAnimationDuration
     */
    private var scrimAnimationDuration: Long
    private var mScrimVisibleHeightTrigger = -1
    private var mOnOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null
    var mCurrentOffset = 0
    var mLastInsets: WindowInsetsCompat? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Add an OnOffsetChangedListener if possible
        val parent = parent
        if (parent is AppBarLayout) {
            // Copy over from the ABL whether we should fit system windows
            fitsSystemWindows = true
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = OffsetUpdateListener()
            }
            parent.addOnOffsetChangedListener(mOnOffsetChangedListener)

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        val parent = parent
        if (mOnOffsetChangedListener != null && parent is AppBarLayout) {
            parent.removeOnOffsetChangedListener(mOnOffsetChangedListener)
        }
        super.onDetachedFromWindow()
    }

    private fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!ObjectsCompat.equals(mLastInsets, newInsets)) {
            mLastInsets = newInsets
            requestLayout()
        }

        /* Consume the insets. This is done so that child views with fitSystemWindows=true do not
         * get the default padding functionality from View */
        return WindowInsetsCompat.CONSUMED
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar()
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim!!.mutate().alpha = mScrimAlpha
            mContentScrim!!.draw(canvas)
        }

        // Let the collapsing text helper draw its text
        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper?.draw(canvas)
        }

        // Now draw the status bar scrim
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            val topInset = if (mLastInsets != null) mLastInsets!!.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
            if (topInset > 0) {
                mStatusBarScrim!!.setBounds(
                    0, -mCurrentOffset, width,
                    topInset - mCurrentOffset
                )
                mStatusBarScrim!!.mutate().alpha = mScrimAlpha
                mStatusBarScrim!!.draw(canvas)
            }
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        /* This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
          but in front of any other children which are behind it. To do this we intercept the
          drawChild() call, and draw our scrim just before the Toolbar is drawn */
        var invalidated = false
        if (mContentScrim != null && mScrimAlpha > 0 && isToolbarChild(child)) {
            mContentScrim!!.mutate().alpha = mScrimAlpha
            mContentScrim!!.draw(canvas)
            invalidated = true
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mContentScrim != null) {
            mContentScrim!!.setBounds(0, 0, w, h)
        }
    }

    private fun ensureToolbar() {
        if (!mRefreshToolbar) {
            return
        }

        // First clear out the current Toolbar
        mToolbar = null
        mToolbarDirectChild = null
        if (mToolbarId != -1) {
            // If we have an ID set, try and find it and it's direct parent to us
            mToolbar = findViewById(mToolbarId)
            if (mToolbar != null) {
                mToolbarDirectChild = findDirectChild(mToolbar)
            }
        }
        if (mToolbar == null) {
            /* If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
              one from our direct children */
            var toolbar: Toolbar? = null
            var i = 0
            val count = childCount
            while (i < count) {
                val child = getChildAt(i)
                if (child is Toolbar) {
                    toolbar = child
                    break
                }
                i++
            }
            mToolbar = toolbar
        }
        updateDummyView()
        mRefreshToolbar = false
    }

    private fun isToolbarChild(child: View): Boolean {
        return if (mToolbarDirectChild == null || mToolbarDirectChild === this) child === mToolbar else child === mToolbarDirectChild
    }

    /**
     * Returns the direct child of this layout, which itself is the ancestor of the
     * given view.
     */
    private fun findDirectChild(descendant: View?): View? {
        var directChild = descendant
        var p = descendant!!.parent
        while (p !== this && p != null) {
            if (p is View) {
                directChild = p
            }
            p = p.parent
        }
        return directChild
    }

    private fun updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            val parent = mDummyView!!.parent
            if (parent is ViewGroup) {
                parent.removeView(mDummyView)
            }
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null) {
                mDummyView = View(context)
            }
            if (mDummyView!!.parent == null) {
                mToolbar!!.addView(
                    mDummyView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mHeightMeasureSpec = heightMeasureSpec
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
        val mode = MeasureSpec.getMode(mHeightMeasureSpec)
        val topInset = if (mLastInsets != null) mLastInsets!!.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            /* If we have a top inset and we're set to wrap_content height we need to make sure
             * we add the top inset to our height, therefore we re-measure */
            mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                measuredHeight + topInset, MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mLastInsets != null) {
            // Shift down any views which are not set to fit system windows
            val insetTop: Int = mLastInsets!!.getInsets(WindowInsetsCompat.Type.systemBars()).top
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.top < insetTop) {
                        /* If the child isn't set to fit system windows but is drawing within
                         * the inset offset it down */
                        ViewCompat.offsetTopAndBottom(child, insetTop)
                    }
                }
                i++
            }
        }
        @SuppressLint("RestrictedApi")
        // Update the collapsed bounds by getting it's transformed bounds
        if (mCollapsingTitleEnabled && mDummyView != null) {
            /* We only draw the title if the dummy view is being displayed (Toolbar removes
              views if there is no space) */
            mDrawCollapsingTitle = (ViewCompat.isAttachedToWindow(mDummyView!!)
                    && mDummyView!!.visibility == VISIBLE)
            if (mDrawCollapsingTitle) {
                val isRtl = (ViewCompat.getLayoutDirection(this)
                        == ViewCompat.LAYOUT_DIRECTION_RTL)

                // Update the collapsed bounds
                val maxOffset = getMaxOffsetForPinChild(
                    (if (mToolbarDirectChild != null) mToolbarDirectChild else mToolbar)!!
                )

                ViewGroupUtils.getDescendantRect(this, mDummyView, mTmpRect)
                mCollapsingTextHelper?.setCollapsedBounds(
                    mTmpRect.left + if (isRtl) mToolbar?.titleMarginEnd
                        ?: 0 else mToolbar?.titleMarginStart
                        ?: 0,
                    mTmpRect.top + maxOffset + (mToolbar?.titleMarginTop ?: 0),
                    mTmpRect.right + if (isRtl) mToolbar?.titleMarginStart
                        ?: 0 else mToolbar?.titleMarginEnd ?: 0,
                    mTmpRect.bottom + maxOffset - (mToolbar?.titleMarginBottom ?: 0)
                )

                // Update the expanded bounds
                mCollapsingTextHelper?.setExpandedBounds(
                    if (isRtl) mExpandedMarginEnd else mExpandedMarginStart,
                    mTmpRect.top + mExpandedMarginTop,
                    right - left - if (isRtl) mExpandedMarginStart else mExpandedMarginEnd,
                    bottom - top - mExpandedMarginBottom
                )
                // Now recalculate using the new bounds
                mCollapsingTextHelper?.recalculate()
            }
        }

        /* Update our child view offset helpers. This needs to be done after the title has been
         * setup, so that any Toolbars are in their original position */
        var i = 0
        val z = childCount
        while (i < z) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout()
            i++
        }

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        if (mToolbar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper?.text)) {
                // If we do not currently have a title, try and grab it from the Toolbar
                mCollapsingTextHelper?.text = mToolbar?.title
            }
            minimumHeight = if (mToolbarDirectChild == null || mToolbarDirectChild === this) {
                getHeightWithMargins(mToolbar)
            } else {
                getHeightWithMargins(mToolbarDirectChild)
            }
        }
        updateScrimVisibility()
    }
    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return `null`.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     */
    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @see .setTitleEnabled
     * @see .getTitle
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     */
    @get:Nullable
    var title: CharSequence?
        get() = if (mCollapsingTitleEnabled) mCollapsingTextHelper?.text else null
        set(title) {
            mCollapsingTextHelper?.text = title
        }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value. Any visibility change will be animated if
     * this view has already been laid out.
     *
     * @param shown whether the scrims should be shown
     *
     * @see .getStatusBarScrim
     * @see .getContentScrim
     */
    private fun setScrimsShown(shown: Boolean) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode)
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value.
     *
     * @param shown whether the scrims should be shown
     * @param animate whether to animate the visibility change
     *
     * @see .getStatusBarScrim
     * @see .getContentScrim
     */
    private fun setScrimsShown(shown: Boolean, animate: Boolean) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(if (shown) 0xFF else 0x0)
            } else {
                scrimAlpha = if (shown) 0xFF else 0x0
            }
            mScrimsAreShown = shown
        }
    }

    private fun animateScrim(targetAlpha: Int) {
        ensureToolbar()
        if (mScrimAnimator == null) {
            mScrimAnimator = ValueAnimator()
            mScrimAnimator!!.duration = scrimAnimationDuration
            mScrimAnimator?.interpolator =
                if (targetAlpha > mScrimAlpha) AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR else AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
            mScrimAnimator!!.addUpdateListener { animator ->
                scrimAlpha = animator.animatedValue as Int
            }
        } else if (mScrimAnimator!!.isRunning) {
            mScrimAnimator!!.cancel()
        }
        mScrimAnimator!!.setIntValues(mScrimAlpha, targetAlpha)
        mScrimAnimator!!.start()
    }

    private var scrimAlpha: Int
        get() = mScrimAlpha
        set(alpha) {
            if (alpha != mScrimAlpha) {
                val contentScrim = mContentScrim
                if (contentScrim != null && mToolbar != null) {
                    ViewCompat.postInvalidateOnAnimation(mToolbar!!)
                }
                mScrimAlpha = alpha
                ViewCompat.postInvalidateOnAnimation(this@CollapsingToolbarLayoutExtension)
            }
        }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see .setContentScrim
     */
    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see .getContentScrim
     */
    @get:Nullable
    var contentScrim: Drawable?
        get() = mContentScrim
        set(drawable) {
            if (mContentScrim !== drawable) {
                if (mContentScrim != null) {
                    mContentScrim!!.callback = null
                }
                mContentScrim = drawable?.mutate()
                if (mContentScrim != null) {
                    mContentScrim!!.setBounds(0, 0, width, height)
                    mContentScrim!!.callback = this
                    mContentScrim!!.alpha = mScrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = drawableState
        var changed = false
        var d = mStatusBarScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        d = mContentScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        if (mCollapsingTextHelper != null) {
            changed = changed or mCollapsingTextHelper.setState(state)
        }
        if (changed) {
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mContentScrim || who === mStatusBarScrim
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        val visible = visibility == VISIBLE
        if (mStatusBarScrim != null && mStatusBarScrim!!.isVisible != visible) {
            mStatusBarScrim!!.setVisible(visible, false)
        }
        if (mContentScrim != null && mContentScrim!!.isVisible != visible) {
            mContentScrim!!.setVisible(visible, false)
        }
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see .setStatusBarScrim
     */
    /**
     * Set the drawable to use for the status bar scrim from resources.
     * Providing null will disable the scrim functionality.
     *
     *
     * This scrim is only shown when we have been given a top system inset.
     *
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see .getStatusBarScrim
     */
    @get:Nullable
    var statusBarScrim: Drawable?
        get() = mStatusBarScrim
        set(drawable) {
            if (mStatusBarScrim !== drawable) {
                if (mStatusBarScrim != null) {
                    mStatusBarScrim!!.callback = null
                }
                mStatusBarScrim = drawable?.mutate()
                if (mStatusBarScrim != null) {
                    if (mStatusBarScrim!!.isStateful) {
                        mStatusBarScrim!!.state = drawableState
                    }
                    DrawableCompat.setLayoutDirection(
                        mStatusBarScrim!!,
                        ViewCompat.getLayoutDirection(this)
                    )
                    mStatusBarScrim!!.setVisible(visibility == VISIBLE, false)
                    mStatusBarScrim!!.callback = this
                    mStatusBarScrim!!.alpha = mScrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will
     * be used when there is extra space in the expanded bounds beyond what is required for
     * the title itself.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    var expandedTitleGravity: Int
        get() = mCollapsingTextHelper?.expandedTextGravity ?: 0
        set(gravity) {
            mCollapsingTextHelper?.expandedTextGravity = gravity
        }

    /* If we reach here then we don't have a min height set. Instead we'll take a
     * guess at 1/3 of our height being visible */
// If we have one explicitly set, return it

    // Otherwise we'll use the default computed value// Update the scrim visibility// If we have a minHeight set, lets use 2 * minHeight (capped at our height)
    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     *
     * If the visible height of this view is less than the given value, the scrims will be
     * made visible, otherwise they are hidden.
     *
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_scrimVisibleHeightTrigger
     */
    /**
     * Returns the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see .setScrimVisibleHeightTrigger
     */
    private var scrimVisibleHeightTrigger: Int
        get() {
            if (mScrimVisibleHeightTrigger >= 0) {
                // If we have one explicitly set, return it
                return mScrimVisibleHeightTrigger
            }

            // Otherwise we'll use the default computed value
            val insetTop = if (mLastInsets != null) mLastInsets!!.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
            val minHeight: Int = ViewCompat.getMinimumHeight(this)
            return if (minHeight > 0) {
                // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
                (minHeight * 2 + insetTop).coerceAtMost(height)
            } else height / 3

            /* If we reach here then we don't have a min height set. Instead we'll take a
             * guess at 1/3 of our height being visible */
        }
        set(height) {
            if (mScrimVisibleHeightTrigger != height) {
                mScrimVisibleHeightTrigger = height
                // Update the scrim visibility
                updateScrimVisibility()
            }
        }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): FrameLayout.LayoutParams {
        return LayoutParams(p)
    }

    class LayoutParams : FrameLayout.LayoutParams {
        /** @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @IntDef(COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        internal annotation class CollapseMode
        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of [.COLLAPSE_MODE_OFF], [.COLLAPSE_MODE_PIN]
         * or [.COLLAPSE_MODE_PARALLAX].
         */
        /**
         * Set the collapse mode.
         *
         */
        @get:CollapseMode
        var collapseMode = COLLAPSE_MODE_OFF
        /**
         * Returns the parallax scroll multiplier used in conjunction with
         * [.COLLAPSE_MODE_PARALLAX].
         *
         * @see .setParallaxMultiplier
         */
        /**
         * Set the parallax scroll multiplier used in conjunction with
         * [.COLLAPSE_MODE_PARALLAX]. A value of `0.0` indicates no movement at all,
         * `1.0f` indicates normal scroll movement.
         *
         *
         * @see .getParallaxMultiplier
         */
        var parallaxMultiplier = DEFAULT_PARALLAX_MULTIPLIER

        @SuppressLint("CustomViewStyleable")
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val typedArray: TypedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CollapsingToolbarLayout_Layout
            )
            collapseMode = typedArray.getInt(
                R.styleable.CollapsingToolbarLayout_Layout_layout_collapseMode,
                COLLAPSE_MODE_OFF
            )
            parallaxMultiplier = typedArray.getFloat(
                R.styleable.CollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier,
                DEFAULT_PARALLAX_MULTIPLIER
            )
            typedArray.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(layoutParams: ViewGroup.LayoutParams) : super(layoutParams)

        companion object {
            private const val DEFAULT_PARALLAX_MULTIPLIER = 0.5f

            /**
             * The view will act as normal with no collapsing behavior.
             */
            const val COLLAPSE_MODE_OFF = 0

            /**
             * The view will pin in place until it reaches the bottom of the
             * [CollapsingToolbarLayoutExtension].
             */
            const val COLLAPSE_MODE_PIN = 1

            /**
             * The view will scroll in a parallax fashion. See [.setParallaxMultiplier]
             * to change the multiplier used.
             */
            const val COLLAPSE_MODE_PARALLAX = 2
        }
    }

    /**
     * Show or hide the scrims if needed
     */
    fun updateScrimVisibility() {
        if (mContentScrim != null || mStatusBarScrim != null) {
            setScrimsShown(height + mCurrentOffset < scrimVisibleHeightTrigger)
        }
    }

    fun getMaxOffsetForPinChild(child: View): Int {
        val offsetHelper: ViewOffsetHelper = getViewOffsetHelper(child)
        val lp = child.layoutParams as LayoutParams
        return (height
                - offsetHelper.layoutTop
                - child.height
                - lp.bottomMargin)
    }

    private inner class OffsetUpdateListener : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout?, verticalOffset: Int) {
            mCurrentOffset = verticalOffset
            val insetTop: Int = if (mLastInsets != null) mLastInsets!!.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
            var i = 0
            val childCount: Int = childCount
            while (i < childCount) {
                val child: View = getChildAt(i)
                val layoutParams: LayoutParams = child.layoutParams as LayoutParams
                val offsetHelper: ViewOffsetHelper = getViewOffsetHelper(child)
                when (layoutParams.collapseMode) {
                    LayoutParams.COLLAPSE_MODE_PIN -> offsetHelper.setTopAndBottomOffset(
                        MathUtils.clamp(
                            -verticalOffset, 0, getMaxOffsetForPinChild(child)
                        )
                    )
                    LayoutParams.COLLAPSE_MODE_PARALLAX -> offsetHelper.setTopAndBottomOffset(
                        (-verticalOffset * layoutParams.parallaxMultiplier).roundToInt()
                    )
                }
                i++
            }

            // Show or hide the scrims if needed
            updateScrimVisibility()
            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(this@CollapsingToolbarLayoutExtension)
            }

            // Update the collapsing text's fraction
            val expandRange: Int = height - ViewCompat.getMinimumHeight(
                this@CollapsingToolbarLayoutExtension
            ) - insetTop
            mCollapsingTextHelper?.expansionFraction =
                abs(verticalOffset) / expandRange.toFloat()
        }
    }

    companion object {
        private const val DEFAULT_SCRIM_ANIMATION_DURATION = 600
        private fun getHeightWithMargins(@NonNull view: View?): Int {
            val lp = view!!.layoutParams
            if (lp is MarginLayoutParams) {
                return view.height + lp.topMargin + lp.bottomMargin
            }
            return view.height
        }

        fun getViewOffsetHelper(view: View): ViewOffsetHelper {
            var offsetHelper: ViewOffsetHelper? =
                view.getTag(R.id.view_offset_helper) as ViewOffsetHelper?
            if (offsetHelper == null) {
                offsetHelper = ViewOffsetHelper(view)
                view.setTag(R.id.view_offset_helper, offsetHelper)
            }
            return offsetHelper
        }
    }

    init {
        ThemeUtils.checkAppCompatTheme(context)
        mCollapsingTextHelper = CollapsingTextHelper(this)
        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR)

        // use own default style
        val mTypedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CollapsingToolbarLayout, defStyleAttr,
            R.style.Widget_Design_MultilineCollapsingToolbar
        )

        mCollapsingTextHelper.expandedTextGravity =
            mTypedArray.getInt(
                R.styleable.CollapsingToolbarLayout_expandedTitleGravity,
                GravityCompat.START or Gravity.BOTTOM
            )
        mCollapsingTextHelper.collapsedTextGravity =
            mTypedArray.getInt(
                R.styleable.CollapsingToolbarLayout_collapsedTitleGravity,
                GravityCompat.START or Gravity.CENTER_VERTICAL
            )

        mExpandedMarginBottom =
            mTypedArray.getDimensionPixelSize(
                R.styleable.CollapsingToolbarLayout_expandedTitleMargin,
                0
            )
        mExpandedMarginEnd = mExpandedMarginBottom
        mExpandedMarginTop = mExpandedMarginEnd
        mExpandedMarginStart = mExpandedMarginTop
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart)) {
            mExpandedMarginStart = mTypedArray.getDimensionPixelSize(
                R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0
            )
        }
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd)) {
            mExpandedMarginEnd = mTypedArray.getDimensionPixelSize(
                R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0
            )
        }
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop)) {
            mExpandedMarginTop = mTypedArray.getDimensionPixelSize(
                R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0
            )
        }
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = mTypedArray.getDimensionPixelSize(
                R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0
            )
        }
        mCollapsingTitleEnabled = mTypedArray.getBoolean(
            R.styleable.CollapsingToolbarLayout_titleEnabled, true
        )
        title = mTypedArray.getText(R.styleable.CollapsingToolbarLayout_title)

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTextAppearance(
            R.style.TextAppearance_Design_CollapsingToolbar_Expanded
        )
        // use own default style
        mCollapsingTextHelper.setCollapsedTextAppearance(R.style.ActionBar_Title)

        // Now overlay any custom text appearances
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTextAppearance(
                mTypedArray.getResourceId(
                    R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, 0
                )
            )
        }
        if (mTypedArray.hasValue(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTextAppearance(
                mTypedArray.getResourceId(
                    R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, 0
                )
            )
        }
        mScrimVisibleHeightTrigger = mTypedArray.getDimensionPixelSize(
            R.styleable.CollapsingToolbarLayout_scrimVisibleHeightTrigger, -1
        )
        scrimAnimationDuration = mTypedArray.getInt(
            R.styleable.CollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION
        ).toLong()
        contentScrim = mTypedArray.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim)
        statusBarScrim = mTypedArray.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim)
        mToolbarId = mTypedArray.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1)
        mTypedArray.recycle()
        setWillNotDraw(false)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> onWindowInsetChanged(insets) }

        //  set the value of maxNumberOfLines attribute to the mCollapsingTextHelper
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CollapsingToolbarLayoutExtension,
            defStyleAttr,
            0
        )
        mCollapsingTextHelper.setMaxLines(
            typedArray.getInteger(
                R.styleable.CollapsingToolbarLayoutExtension_maxLines,
                3
            )
        )
        mCollapsingTextHelper.setLineSpacingExtra(
            typedArray.getFloat(
                R.styleable.CollapsingToolbarLayoutExtension_lineSpacingExtra,
                0f
            )
        )
        mCollapsingTextHelper.setLineSpacingMultiplier(
            typedArray.getFloat(
                R.styleable.CollapsingToolbarLayoutExtension_lineSpacingMultiplier,
                1f
            )
        )
        typedArray.recycle()
    }
}
