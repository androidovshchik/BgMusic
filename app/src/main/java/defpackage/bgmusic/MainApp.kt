package defpackage.bgmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import defpackage.bgmusic.extension.isOreoPlus
import defpackage.bgmusic.extension.startForegroundService
import org.jetbrains.anko.notificationManager
import timber.log.Timber

enum class Mode(val id: Int) {
    SERVICE(0),
    WORKER_ONE_TIME(1),
    WORKER_PERIODIC(2);
}

@Suppress("unused")
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
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
        when (BuildConfig.MODE) {
            Mode.SERVICE.id -> {
                startForegroundService<MusicService>()
            }
            Mode.WORKER_ONE_TIME.id -> {
            }
            Mode.WORKER_PERIODIC.id -> {
            }
        }
    }
}