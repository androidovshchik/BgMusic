package defpackage.bgmusic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service() {

    private val player by lazy { MusicPlayer(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            Int.MAX_VALUE, NotificationCompat.Builder(applicationContext, "service")
                .setSmallIcon(R.drawable.icon)
                .setSound(null)
                .build()
        )
        player.startPlay()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}