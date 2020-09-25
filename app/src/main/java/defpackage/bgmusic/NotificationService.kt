package defpackage.bgmusic

import android.media.session.MediaController
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import timber.log.Timber

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(notification: StatusBarNotification) {
        logNotification(notification)
        cancelNotification()
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {}

    private fun logNotification(notification: StatusBarNotification) {
        val classname = javaClass.simpleName
        LogUtil.logDivider(classname, ":")
        LogUtil.logCentered(" ", classname, "New notification")
        LogUtil.logCentered(":", classname, "packageName: " + notification.packageName)
        LogUtil.logCentered(":", classname, "id: " + notification.id)
        if (notification.notification.actions != null) {
            LogUtil.logCentered(" ", classname, "Notification actions")
            for (action in notification.notification.actions) {
                LogUtil.logCentered(":", classname, "action.title: " + action.title)
            }
        }
        if (notification.notification.extras != null) {
            LogUtil.logCentered(" ", classname, "Notification extras")
            for (key in notification.notification.extras.keySet()) {
                Timber.d(key + ": " + notification.notification.extras[key])
            }
        }
        notification.notification.extras.getParcelable<MediaSession.Token>("android.mediaSession")
            ?.let {
                val mediaController = MediaController(applicationContext, it)
                Timber.e("mc1 ${mediaController.packageName}")
                Timber.e("mc2 ${mediaController.sessionActivity?.javaClass?.name}")
                Timber.e("mc2 ${mediaController.playbackState}")
            }
        notification.notification.extras.get("android.compactActions")?.let {
            Timber.e("compactActions ${it.javaClass.name}")
        }
        LogUtil.logDivider(classname, ":")
    }
}