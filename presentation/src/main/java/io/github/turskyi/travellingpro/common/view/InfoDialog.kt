package io.github.turskyi.travellingpro.common.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import io.github.turskyi.travellingpro.R

class InfoDialog : AppCompatDialogFragment() {
    companion object {
        const val ARG_INFO = "ua.turskyi.travelling.ARG_INFO"

        fun newInstance(info: String): InfoDialog {
            val fragment = InfoDialog()
            val bundle = Bundle().apply {
                putString(ARG_INFO, info)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(arguments?.getString(ARG_INFO))
            .setPositiveButton(getString(R.string.dialog_btn_ok)) { _, _ ->
                dismiss()
            }
        return builder.create()
    }
}