package io.github.turskyi.travellingpro.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(@StringRes msgResId: Int) = context?.toast(msgResId)
fun Fragment.toastLong(@StringRes msgResId: Int) = context?.toastLong(msgResId)