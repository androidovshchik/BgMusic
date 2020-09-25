package defpackage.bgmusic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service(), CoroutineScope {

    private val job = SupervisorJob()

    private val player by lazy { MusicPlayer(applicationContext) }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            1, NotificationCompat.Builder(applicationContext, "service")
                .setSmallIcon(R.drawable.icon)
                .setContent(RemoteViews(packageName, R.layout.notification))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSound(null)
                .build()
        )
        player.startPlay()
        launch {
            while (true) {
                delay(5_000)
                delay(5_000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        job.cancelChildren()
        player.release()
        super.onDestroy()
    }

    override val coroutineContext = Dispatchers.Main + job + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}