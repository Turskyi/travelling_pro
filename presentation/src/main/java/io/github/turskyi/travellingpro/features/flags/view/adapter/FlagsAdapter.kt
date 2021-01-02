package io.github.turskyi.travellingpro.features.flags.view.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.turskyi.travellingpro.features.flags.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.fragment.FlagFragment

class FlagsAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity),
    LifecycleObserver {
    private var flagsActivityViewListener: FlagsActivityView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        try {
            flagsActivityViewListener = recyclerView.context as FlagsActivityView?
        } catch (castException: ClassCastException) {
            /* in this case the activity does not implement the listener.  */
            castException.printStackTrace()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifeCycleDestroy() {
        flagsActivityViewListener = null
    }

    override fun getItemCount(): Int = flagsActivityViewListener?.getItemCount() ?: 0

    override fun createFragment(position: Int): Fragment = FlagFragment().apply {
        arguments = bundleOf(
            EXTRA_POSITION to position
        )
    }
}