package defpackage.bgmusic

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import defpackage.bgmusic.extension.getComponent
import defpackage.bgmusic.extension.isRPlus
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
    }

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun requestPermission() {
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
        if (!listeners.contains(packageName)) {
            val name = getComponent<NotificationService>().flattenToString()
            if (isRPlus()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                    putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, name)
                })
            } else {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    putExtra(EXTRA_FRAGMENT_ARG_KEY, name)
                    putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, Bundle().also {
                        it.putString(EXTRA_FRAGMENT_ARG_KEY, name)
                    })
                })
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

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    // see https://android.googlesource.com/platform/packages/apps/Settings/+/master/src/com/android/settings/SettingsActivity.java
    companion object {

        private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

        private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
    }
}

class WebClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return false
    }
}