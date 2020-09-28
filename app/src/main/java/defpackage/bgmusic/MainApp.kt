package defpackage.bgmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import defpackage.bgmusic.extension.isOreoPlus
import defpackage.bgmusic.player.MusicWorker
import defpackage.bgmusic.player.ServiceRunnable
import org.jetbrains.anko.notificationManager
import timber.log.Timber
import java.io.File

@Suppress("unused")
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(LogTree(File(getExternalFilesDir(null), "logs")))
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
        when {
            BuildConfig.FLAVOR.startsWith("service") -> {
                ServiceRunnable(applicationContext).run()
            }
            BuildConfig.FLAVOR.startsWith("worker") -> {
                MusicWorker.launch(applicationContext)
            }
        }
    }
}