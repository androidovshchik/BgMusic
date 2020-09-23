package defpackage.bgmusic

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import defpackage.bgmusic.extension.isOreoPlus

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service() {

    private val player by lazy { MusicPlayer(applicationContext) }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (isOreoPlus()) {
            startForeground(
                1, NotificationCompat.Builder(applicationContext, "service")
                    .setSmallIcon(R.drawable.icon)
                    .setContent(RemoteViews(packageName, R.layout.notification))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setSound(null)
                    .build()
            )
        } else {
            startForeground(1, Notification())
        }
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