package defpackage.bgmusic.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.observeForeverFreshly
import androidx.lifecycle.removeFreshObserver
import defpackage.bgmusic.R
import defpackage.bgmusic.extension.isRunning
import defpackage.bgmusic.extension.startForegroundService
import defpackage.bgmusic.playbackChanges
import kotlinx.coroutines.Runnable
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.startService
import org.jetbrains.anko.stopService
import timber.log.Timber
import java.lang.ref.WeakReference

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service(), Observer<Boolean> {

    private val holder by lazy { ContextHolder(applicationContext) }

    private val player by lazy { MusicPlayer(holder, applicationContext) }

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
        Timber.d("Starting foreground service")
        player.preparePlaylist()
        player.startPlay()
        playbackChanges.observeForeverFreshly(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onChanged(hasPause: Boolean) {
        if (hasPause) {
            player.startPlay()
        }
    }

    override fun onDestroy() {
        holder.saveProgress(player.track, player.position)
        playbackChanges.removeFreshObserver(this)
        player.release()
        super.onDestroy()
    }

    companion object {

        /**
         * @return true if service is running
         */
        fun start(context: Context, vararg params: Pair<String, Any?>): Boolean {
            with(context) {
                return if (!activityManager.isRunning<MusicService>()) {
                    startForegroundService<MusicService>() != null
                } else {
                    startService<MusicService>(*params) != null
                }
            }
        }

        /**
         * @return true if service is stopped
         */
        @Suppress("unused")
        fun stop(context: Context): Boolean {
            with(context) {
                if (activityManager.isRunning<MusicService>()) {
                    return stopService<MusicService>()
                }
            }
            return true
        }
    }
}

class ServiceRunnable(context: Context) : Runnable {

    private val reference = WeakReference(context)

    private val handler = Handler(Looper.getMainLooper())

    override fun run() {
        try {
            reference.get()?.let {
                MusicService.start(it)
            }
        } catch (e: SecurityException) {
            Timber.e(e)
            handler.postDelayed(this, 1000)
        }
    }
}