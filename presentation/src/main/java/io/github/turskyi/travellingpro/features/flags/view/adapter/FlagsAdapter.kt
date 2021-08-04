package io.github.turskyi.travellingpro.features.flags.view.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.features.flags.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_USER
import io.github.turskyi.travellingpro.features.flags.view.fragments.FlagFragment
import io.github.turskyi.travellingpro.features.flags.view.fragments.FriendFlagsFragment
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong

class FlagsAdapter(private val activity: AppCompatActivity) :
    FragmentStateAdapter(activity),
    LifecycleObserver {
    private var flagsActivityViewListener: FlagsActivityView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        try {
            val context: Context = recyclerView.context
            if (context is FlagsActivityView) {
                flagsActivityViewListener = context
            } else {
                showReportDialog(recyclerView, context)
            }
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            recyclerView.toastLong(
                castException.localizedMessage ?: castException.stackTraceToString()
            )
        }
    }

    private fun showReportDialog(
        recyclerView: RecyclerView,
        context: Context
    ) {
        val lastDialog: AlertDialog = AlertDialog.Builder(recyclerView.context)
            .setTitle(context.getString(R.string.alert_error_title))
            .setMessage(context.getString(R.string.alert_error_message))
            .setCancelable(false)
            .setPositiveButton(
                context.getString(R.string.yes)
            ) { _: DialogInterface?, _: Int ->
                val intent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts(
                        context.getString(R.string.scheme_mailto),
                        context.getString(R.string.email),
                        // Gets the decoded fragment part of this URI, everything after the '#', null if undefined.
                        null
                    )
                )
                intent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.error_message_intro) + this.toString(),
                )
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }.setNegativeButton(
                context.getString(R.string.no)
            ) { dialog: DialogInterface, _: Int ->
                context.toast(R.string.error_message_try_tomorrow)
                dialog.cancel()
                val newIntent = Intent(context, context.javaClass)
                context.startActivity(newIntent)
            }.create()
        lastDialog.show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifeCycleDestroy() {
        flagsActivityViewListener = null
    }

    override fun getItemCount(): Int = flagsActivityViewListener?.getItemCount() ?: 0

    override fun createFragment(position: Int): Fragment {
        return if (activity.intent.extras != null && activity.intent.extras!!.getParcelable<Traveller>(EXTRA_USER) != null) {
            val traveller: Traveller = activity.intent.extras!!.getParcelable(EXTRA_USER)!!
            FriendFlagsFragment().apply {
                arguments = bundleOf(
                    EXTRA_POSITION to position,
                    EXTRA_USER to traveller,
                )
            }
        } else {
            FlagFragment().apply {
                arguments = bundleOf(EXTRA_POSITION to position)
            }
        }
    }
}