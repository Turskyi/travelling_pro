package io.github.turskyi.travellingpro.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import com.google.android.material.snackbar.Snackbar
import io.github.turskyi.travellingpro.BuildConfig
import io.github.turskyi.travellingpro.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun View.setDynamicVisibility(visibility: Boolean) = if (visibility) {
    this.animate().alpha(1.0f).duration = 2000
} else {
    this.animate().alpha(0.0f).duration = 200
}

fun View.convertViewToBitmap(): Bitmap {
    val bitmap: Bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}

fun View.getScreenShot(): Bitmap = Bitmap.createBitmap(convertViewToBitmap())

/**
 * Show a snackbar with [messageRes]
 */
fun View.showShortMsg(@StringRes messageRes: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    val mSnackbar: Snackbar = Snackbar.make(this, messageRes, duration)
    mSnackbar.config(this.context)
    mSnackbar.show()
}

fun View.showSnackBar(@StringRes msgString: Int) = showShortMsg(msgString)

inline fun View.longSnackWithAction(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    function: Snackbar.() -> Unit
) {
    val snack = Snackbar.make(this, message, length)
    snack.config(this.context)
    val snackView = snack.view
    val snackTextView =
        snackView.findViewById<TextView>(R.id.snackbar_text)
    snackTextView?.setTextColor(Color.RED)
    snack.function()
    snack.show()
}

inline fun View.showSnackWithAction(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    function: Snackbar.() -> Unit
) = longSnackWithAction(message, length, function)

fun View.toast(@StringRes msgResId: Int) = context.toast(msgResId)
fun View.toastLong(msg: String) = Toast.makeText(this.context, msg, Toast.LENGTH_LONG).show()

fun View.shareImageViaChooser() {
    val fileName: String = resources.getString(
        R.string.picture_file_name,
        SimpleDateFormat(
            resources.getString(R.string.day_month_year),
            Locale.ENGLISH
        ).format(Date())
    )
    val bitmap: Bitmap = getScreenShot()
    val file: File = bitmap.convertBitmapToFile(context, fileName)
    val uri: Uri? = FileProvider.getUriForFile(
        context,
        context.packageName.toString() + context.resources.getString(R.string.file_provider),
        file
    )

    val intentImage = Intent()
    intentImage.action = Intent.ACTION_SEND
    intentImage.type = resources.getString(R.string.image_type)
    intentImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intentImage.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
    intentImage.putExtra(
        Intent.EXTRA_TEXT, resources.getString(R.string.share_massage, BuildConfig.APPLICATION_ID)
    )
    intentImage.putExtra(Intent.EXTRA_STREAM, uri)
    val chooser = Intent.createChooser(
        intentImage,
        context.getString(R.string.share_title)
    )

    val resInfoList: List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                chooser,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        }

    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        context.grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
    try {
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        toast(R.string.msg_no_app_installed)
    }
}


fun View.shareViaFacebook(fragment: Fragment) {
    val webAddress: ShareHashtag =
        ShareHashtag.Builder()
            .setHashtag(resources.getString(R.string.share_massage, BuildConfig.APPLICATION_ID))
            .build()
    val bitmap: Bitmap = getScreenShot()
    val sharePhoto = SharePhoto.Builder().setBitmap(bitmap).setCaption(
        resources.getString(
            R.string.picture_name, SimpleDateFormat(
                context.getString(R.string.day_month_year),
                Locale.ENGLISH
            ).format(Date())
        )
    )
        .build()
    val mediaContent: ShareMediaContent = ShareMediaContent.Builder()
        .addMedium(sharePhoto)
        .setShareHashtag(webAddress)
        .build()
    ShareDialog.show(fragment, mediaContent)
}

