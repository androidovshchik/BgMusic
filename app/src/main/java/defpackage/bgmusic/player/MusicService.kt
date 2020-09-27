package defpackage.bgmusic.player

import android.app.AlarmManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.observeForeverFreshly
import androidx.lifecycle.removeFreshObserver
import defpackage.bgmusic.Preferences
import defpackage.bgmusic.R
import defpackage.bgmusic.extension.cancelAlarm
import defpackage.bgmusic.extension.isRunning
import defpackage.bgmusic.extension.pendingReceiverFor
import defpackage.bgmusic.extension.startForegroundService
import defpackage.bgmusic.playbackChanges
import defpackage.bgmusic.service.RestartReceiver
import kotlinx.coroutines.*
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.startService
import org.jetbrains.anko.stopService
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service(), CoroutineScope, IHolder, Observer<Boolean> {

    private val job = SupervisorJob()

    private val preferences by lazy { Preferences(applicationContext) }

    private val player by lazy { MusicPlayer(this, applicationContext) }

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
        player.preparePlaylist(preferences.track, preferences.position)
        player.startPlay()
        playbackChanges.observeForeverFreshly(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun saveProgress(track: Int) {
        Timber.d("Saving track=$track")
        preferences.track = track
    }

    override fun setAlarmIfNeeded() {
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
        if (!listeners.contains(packageName)) {
            val delay = TimeUnit.MINUTES.toMillis(10)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay,
                pendingReceiverFor<RestartReceiver>()
            )
        }
    }

    override fun cancelAlarm() {
        cancelAlarm<RestartReceiver>()
    }

    override fun onChanged(hasPause: Boolean) {
        if (hasPause) {
            player.startPlay()
        }
    }

    override fun onDestroy() {
        val position = player.position
        Timber.d("Saving position=$position")
        preferences.position = position
        playbackChanges.removeFreshObserver(this)
        job.cancelChildren()
        player.release()
        super.onDestroy()
    }

    override val coroutineContext = Dispatchers.Main + job + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
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

    override fun run() {
        try {
            reference.get()?.let {
                MusicService.start(it)
            }
        } catch (e: SecurityException) {
            Timber.e(e)
            @Suppress("DEPRECATION")
            Handler().postDelayed(this, 1000)
        }
    }
}