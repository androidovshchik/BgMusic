package defpackage.bgmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.jakewharton.threetenabp.AndroidThreeTen
import defpackage.bgmusic.extension.isOreoPlus
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService
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
                NotificationChannel("service", "Default", NotificationManager.IMPORTANCE_LOW)
            )
        }
        AndroidThreeTen.init(this)
        when (BuildConfig.MODE) {
            Mode.SERVICE.id -> {
                startService<MusicService>()
            }
            Mode.WORKER_ONE_TIME.id -> {
            }
            Mode.WORKER_PERIODIC.id -> {
            }
        }
    }
}