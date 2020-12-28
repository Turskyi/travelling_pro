package io.github.turskyi.travellingpro.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    /* Finds the currently focused view, so we can grab the correct window token from it. */
    var view = currentFocus
    /* If no view currently does not have a focus, create a new one, just so we can grab a window
     token from it */
    view ?: run {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
    view?.clearFocus()
}

fun AppCompatActivity.showKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}