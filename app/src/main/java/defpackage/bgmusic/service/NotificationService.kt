package defpackage.bgmusic.service

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import defpackage.bgmusic.playbackChanges
import timber.log.Timber
import java.util.*

class NotificationService : NotificationListenerService() {

    private var has = false

    override fun onNotificationPosted(notification: StatusBarNotification) {
        logNotification(notification)
        notification.notification.extras.getParcelable<MediaSession.Token>("android.mediaSession")
            ?.let {
                val mediaController = MediaController(applicationContext, it)
                when (mediaController.playbackState?.state) {
                    PlaybackState.STATE_PAUSED -> playbackChanges.postValue(true)
                    PlaybackState.STATE_PLAYING -> playbackChanges.postValue(false)
                }
                Timber.e("mc1 ${mediaController.packageName}")
                Timber.e("mc2 ${mediaController.sessionActivity?.javaClass?.name}")
                Timber.e("mc2 ${mediaController.playbackState}")
            }
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {}

    private fun logNotification(notification: StatusBarNotification) {
        val classname = javaClass.simpleName
        Beauty.logDivider(classname, ":")
        Beauty.logCentered(" ", classname, "New notification")
        Beauty.logCentered(":", classname, "packageName: " + notification.packageName)
        Beauty.logCentered(":", classname, "id: " + notification.id)
        if (notification.notification.actions != null) {
            Beauty.logCentered(" ", classname, "Notification actions")
            for (action in notification.notification.actions) {
                Beauty.logCentered(":", classname, "action.title: " + action.title)
            }
        }
        if (notification.notification.extras != null) {
            Beauty.logCentered(" ", classname, "Notification extras")
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
        Beauty.logDivider(classname, ":")
    }
}

object Beauty {

    private const val STYLED_LOG_LENGTH = 48

    private fun tag(tag: String): Timber.Tree {
        return Timber.tag(tag)
    }

    fun logCentered(character: String, tag: String, text: String) {
        var text = text
        val length = text.length + 2
        val edge: String
        if (length >= STYLED_LOG_LENGTH) {
            edge = ""
            text = (character + "%s" + text.substring(0, STYLED_LOG_LENGTH - 5)
                    + "%s" + "..." + character)
        } else {
            edge = repeat(" ", (STYLED_LOG_LENGTH - length) / 2)
            text = character + "%s" + text + "%s" + (if (length % 2 == 0) "" else " ") + character
        }
        log(tag, text, edge)
    }

    private fun log(tag: String, text: String, edge: String) {
        tag(tag).i(text, edge, edge)
    }

    fun logDivider(tag: String, character: String) {
        tag(tag).i(repeat(character, STYLED_LOG_LENGTH))
    }

    private fun repeat(what: String, times: Int): String {
        return TextUtils.join(", ", Collections.nCopies(times, what))
    }
}