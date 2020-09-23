package defpackage.bgmusic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service() {

    private val player by lazy { MusicPlayer(applicationContext) }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            Int.MAX_VALUE, NotificationCompat.Builder(applicationContext, "service")
                .setSmallIcon(R.drawable.icon)
                .setContent(RemoteViews(packageName, R.layout.notification))
                .setSound(null)
                .build()
        )
        player.startPlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}