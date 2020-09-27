package defpackage.bgmusic.player

import android.app.AlarmManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.observeForeverFreshly
import androidx.lifecycle.removeFreshObserver
import defpackage.bgmusic.R
import defpackage.bgmusic.extension.pendingReceiverFor
import defpackage.bgmusic.playbackChanges
import defpackage.bgmusic.service.RestartReceiver
import kotlinx.coroutines.*
import org.jetbrains.anko.alarmManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service(), CoroutineScope, IHolder, Observer<Boolean> {

    private val job = SupervisorJob()

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
        player.preparePlaylist()
        player.startPlay()
        playbackChanges.observeForeverFreshly(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
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

    override fun onChanged(hasPause: Boolean) {
        if (hasPause) {
            player.startPlay()
        }
    }

    override fun onDestroy() {
        playbackChanges.removeFreshObserver(this)
        job.cancelChildren()
        player.release()
        super.onDestroy()
    }

    override val coroutineContext = Dispatchers.Main + job + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}