package io.github.turskyi.travellingpro.utils.extensions

import android.graphics.Color
import android.text.Html
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

fun String.isHttpUrl(): Boolean =
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)

fun TextView.showFlagText(flag: String) {
    text = flag
    visibility = View.VISIBLE
}

fun WebView.loadFlagFallback(flag: String) {
    webViewClient = WebViewClient()
    visibility = View.VISIBLE
    setBackgroundColor(Color.TRANSPARENT)

    val flagContent = if (flag.isHttpUrl()) {
        "<img src='${Html.escapeHtml(flag)}'/>"
    } else {
        "<span>${Html.escapeHtml(flag)}</span>"
    }
    loadData(
        "<html><head><meta charset='utf-8'><style>" +
                "body{margin:0;text-align:center;} img{width:80%;} " +
                "span{font-size:30px;}" +
                "</style></head><body>$flagContent</body></html>",
        "text/html",
        "UTF-8",
    )
}
