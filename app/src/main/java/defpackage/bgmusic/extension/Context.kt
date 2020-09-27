package defpackage.bgmusic.extension

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.os.SystemClock
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startService

inline fun <reified T> Context.getComponent() = ComponentName(applicationContext, T::class.java)

inline fun <reified T> Context.activityCallback(action: T.() -> Unit) {
    getActivity()?.let {
        if (it is T && !it.isFinishing) {
            action(it)
        }
    }
}

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

fun Context.areGranted(vararg permissions: String): Boolean {
    for (permission in permissions) {
        if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

inline fun <reified T : Service> Context.startForegroundService(vararg params: Pair<String, Any?>): ComponentName? {
    return if (isOreoPlus()) {
        startForegroundService(intentFor<T>(*params))
    } else {
        startService<T>(*params)
    }
}

inline fun <reified T : Activity> Context.pendingActivityFor(
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    vararg params: Pair<String, Any?>
): PendingIntent =
    PendingIntent.getActivity(applicationContext, requestCode, intentFor<T>(*params), flags)

inline fun <reified T : BroadcastReceiver> Context.pendingReceiverFor(
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    vararg params: Pair<String, Any?>
): PendingIntent =
    PendingIntent.getBroadcast(applicationContext, requestCode, intentFor<T>(*params), flags)

fun Context.pendingReceiverFor(
    action: String,
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
): PendingIntent =
    PendingIntent.getBroadcast(applicationContext, requestCode, Intent(action), flags)

inline fun <reified T : BroadcastReceiver> Context.createAlarm(requestCode: Int, interval: Long) {
    when {
        isMarshmallowPlus() -> alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval,
            pendingReceiverFor<T>(requestCode)
        )
        else -> alarmManager.setExact(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval,
            pendingReceiverFor<T>(requestCode)
        )
    }
}

inline fun <reified T : BroadcastReceiver> Context.cancelAlarm(
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    vararg params: Pair<String, Any?>
) {
    alarmManager.cancel(pendingReceiverFor<T>(requestCode, flags))
}