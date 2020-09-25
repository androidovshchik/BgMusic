package defpackage.bgmusic

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import defpackage.bgmusic.extension.isLollipopMR1Plus
import org.jetbrains.anko.UI
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.webView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val webClient = WebClient()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(UI {
            frameLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
                webView = webView {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                    }
                    webViewClient = webClient
                    CookieManager.getInstance().also {
                        it.setAcceptCookie(true)
                        it.setAcceptThirdPartyCookies(this, true)
                    }
                }.lparams(matchParent, matchParent)
            }
        }.view)
        webView.loadUrl("https://yandex.ru")
        requirePermission()
    }

    override fun onStart() {
        super.onStart()
        if (isLollipopMR1Plus()) {
            requirePermission()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun requirePermission() {
        if (!isNotificationServiceEnabled()) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            } catch (ignored: Throwable) {
                // api level < 22
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onStop() {
        CookieManager.getInstance().flush()
        super.onStop()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrBlank()) {
            flat.split(":").forEach {
                val component = ComponentName.unflattenFromString(it)
                if (packageName == component?.packageName) {
                    return true
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}

class WebClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return false
    }
}