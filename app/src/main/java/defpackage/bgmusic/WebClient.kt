package defpackage.bgmusic

import android.webkit.WebView
import android.webkit.WebViewClient

class WebClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return false
    }
}