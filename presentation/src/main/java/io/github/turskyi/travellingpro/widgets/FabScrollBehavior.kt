package io.github.turskyi.travellingpro.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.turskyi.travellingpro.utils.extensions.getToolbarHeight

class FabScrollBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<FloatingActionButton?>(context, attrs) {
    private val toolbarHeight: Int = context.getToolbarHeight()
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        fab: FloatingActionButton,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        fab: FloatingActionButton,
        dependency: View
    ): Boolean {
        if (dependency is AppBarLayout) {
            val layoutParams: CoordinatorLayout.LayoutParams =
                fab.layoutParams as CoordinatorLayout.LayoutParams
            val fabBottomMargin: Int = layoutParams.bottomMargin
            val distanceToScroll = fab.height + fabBottomMargin
            val ratio = dependency.getY() / toolbarHeight.toFloat()
            /* change the following data depending on the result is needed */
            fab.translationY = -distanceToScroll * 3 * ratio
        }
        return true
    }
}