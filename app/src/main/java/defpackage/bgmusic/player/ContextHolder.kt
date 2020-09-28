package defpackage.bgmusic.player

import android.app.AlarmManager
import android.content.Context
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationManagerCompat
import com.chibatching.kotpref.bulk
import defpackage.bgmusic.Preferences
import defpackage.bgmusic.extension.cancelAlarm
import defpackage.bgmusic.extension.pendingReceiverFor
import defpackage.bgmusic.service.RestartReceiver
import org.jetbrains.anko.alarmManager
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class ContextHolder(context: Context) : IHolder {

    private val reference = WeakReference(context)

    private val preferences = Preferences(context)

    override val track: Int
        get() = preferences.track

    override val position: Long
        get() = preferences.position

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun saveProgress(_track: Int, _position: Long) {
        reference.get()?.run {
            Timber.d("Saving track=$_track position=$_position")
            preferences.bulk {
                track = _track
                position = _position
            }
        }
    }

    override fun setAlarmIfNeeded() {
        reference.get()?.run {
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
    }

    override fun cancelAlarm() {
        reference.get()?.run {
            cancelAlarm<RestartReceiver>()
        }
    }
}