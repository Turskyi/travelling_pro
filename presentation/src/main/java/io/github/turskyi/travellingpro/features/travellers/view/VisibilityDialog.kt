package io.github.turskyi.travellingpro.features.travellers.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import io.github.turskyi.travellingpro.R

class VisibilityDialog : AppCompatDialogFragment() {
    companion object {
        const val ARG_INFO = "ua.turskyi.travelling.ARG_INFO"

        fun newInstance(info: String): VisibilityDialog {
            val fragment = VisibilityDialog()
            val bundle = Bundle().apply { putString(ARG_INFO, info) }
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var listener: VisibilityListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as VisibilityListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement VisibilityListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(arguments?.getString(ARG_INFO))
            .setPositiveButton(getString(R.string.dialog_btn_ok_ready)) { _, _ ->
                listener.becomeVisible()
            }.setNegativeButton(getString(R.string.dialog_btn_not_ok)) { _, _ ->
                dismiss()
            }
        return builder.create()
    }

    interface VisibilityListener {
        fun becomeVisible()
    }
}