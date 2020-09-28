package defpackage.bgmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import defpackage.bgmusic.extension.isOreoPlus
import defpackage.bgmusic.player.ServiceRunnable
import org.jetbrains.anko.notificationManager
import timber.log.Timber

@Suppress("unused")
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(LogTree())
        if (isOreoPlus()) {
            notificationManager.createNotificationChannel(
                NotificationChannel("main", "Main", NotificationManager.IMPORTANCE_DEFAULT)
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "service",
                    "Service",
                    NotificationManager.IMPORTANCE_NONE
                ).apply {
                    lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
                    setSound(null, null)
                }
            )
        }
        AndroidThreeTen.init(this)
        when (BuildConfig.FLAVOR) {
            "service" -> {
                ServiceRunnable(applicationContext).run()
            }
            "worker" -> {
            }
        }
    }
}