package io.github.turskyi.travellingpro.features.flags.view.adapter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.features.flags.view.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_USER
import io.github.turskyi.travellingpro.features.flags.view.fragments.FlagFragment
import io.github.turskyi.travellingpro.features.flags.view.fragments.FriendFlagsFragment
import io.github.turskyi.travellingpro.utils.extensions.showReportDialog
import io.github.turskyi.travellingpro.utils.extensions.toastLong

class FlagsAdapter(private val activity: AppCompatActivity) :
    FragmentStateAdapter(activity),
    LifecycleEventObserver {
    private var flagsActivityViewListener: FlagsActivityView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        try {
            val context: Context = recyclerView.context
            if (context is FlagsActivityView) {
                flagsActivityViewListener = context
            } else {
                context.showReportDialog()
            }
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            recyclerView.toastLong(
                castException.localizedMessage ?: castException.stackTraceToString()
            )
        }
    }

    override fun getItemCount(): Int = flagsActivityViewListener?.getItemCount() ?: 0

    override fun createFragment(position: Int): Fragment {
        return if (activity.intent.extras != null && activity.intent.extras!!.getParcelable<Traveller>(
                EXTRA_USER
            ) != null
        ) {
            val traveller: Traveller = activity.intent.extras!!.getParcelable(EXTRA_USER)!!
            FriendFlagsFragment().apply {
                arguments = bundleOf(
                    EXTRA_POSITION to position,
                    EXTRA_USER to traveller,
                )
            }
        } else {
            FlagFragment().apply { arguments = bundleOf(EXTRA_POSITION to position) }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            flagsActivityViewListener = null
        }
    }
}