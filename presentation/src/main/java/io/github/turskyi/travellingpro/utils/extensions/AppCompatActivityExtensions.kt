package io.github.turskyi.travellingpro.utils.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.common.view.InfoDialog

fun AppCompatActivity.openInfoDialog(info: String) {
    val infoDialog = InfoDialog.newInstance(info)
    infoDialog.show(this.supportFragmentManager, resources.getString(R.string.tag_info_dialog))
}

fun AppCompatActivity.openInfoDialog(@StringRes info: Int) {
    val infoDialog: InfoDialog = InfoDialog.newInstance(getString(info))
    infoDialog.show(this.supportFragmentManager, resources.getString(R.string.tag_info_dialog))
}

fun AppCompatActivity.openInfoDialog(@StringRes info: Int, textArg: String) {
    val infoDialog: InfoDialog = InfoDialog.newInstance(getString(info, textArg))
    infoDialog.show(this.supportFragmentManager, resources.getString(R.string.tag_info_dialog))
}

fun AppCompatActivity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Finds the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    /* If no view currently does not have a focus, create a new one, just so we can grab a window
     * token from it */
    view ?: run {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
    view?.clearFocus()
}

fun AppCompatActivity.showKeyboard() {
    val inputMethodManager: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Finds the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    /* If no view currently does not have a focus, create a new one, just so we can grab a window
     token from it */
    if(view == null){
        view = View(this)
    }
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}