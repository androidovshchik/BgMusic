package defpackage.bgmusic.service

import android.annotation.SuppressLint
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.UiThread
import defpackage.bgmusic.BeautyCat
import defpackage.bgmusic.BuildConfig
import defpackage.bgmusic.playbackChanges

class NotificationService : NotificationListenerService() {

    @UiThread
    @SuppressLint("SwitchIntDef")
    override fun onNotificationPosted(notification: StatusBarNotification) {
        if (BuildConfig.DEBUG) {
            logNotification(notification)
        }
        notification.notification.extras.getParcelable<MediaSession.Token>("android.mediaSession")
            ?.let {
                val mediaController = MediaController(applicationContext, it)
                when (mediaController.playbackState?.state) {
                    PlaybackState.STATE_PAUSED -> {
                        playbackChanges.value = true
                    }
                }
            }
    }

    @UiThread
    override fun onNotificationRemoved(notification: StatusBarNotification) {
    }

    private fun logNotification(notification: StatusBarNotification) {
        val char = chars[chars.indices.random()]
        BeautyCat.div(tag, char)
        BeautyCat.log(tag, "New notification")
        BeautyCat.log(tag, "id: ${notification.id}", char)
        BeautyCat.log(tag, "packageName: ${notification.packageName}", char)
        notification.notification.actions?.let {
            BeautyCat.log(tag, "Notification actions")
            it.forEachIndexed { i, action ->
                BeautyCat.log(tag, "action.title[$i]: ${action.title}", char)
            }
        }
        notification.notification.extras?.let {
            BeautyCat.log(tag, "Notification extras")
            BeautyCat.map(tag, it, char)
        }
        BeautyCat.div(tag, char)
    }

    companion object {

        private val tag = NotificationService::class.java.simpleName

        private val chars = arrayOf("*", ":", ";", "$", "#", "@", "&", "=", "\\", "/")
    }
}