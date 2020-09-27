package defpackage.bgmusic.service

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
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
                    PlaybackState.STATE_PAUSED -> {
                    }//playbackChanges.postValue(true)
                    PlaybackState.STATE_PLAYING -> {
                    }//playbackChanges.postValue(false)
                }
                Timber.e("mc1 ${mediaController.packageName}")
                Timber.e("mc2 ${mediaController.sessionActivity?.javaClass?.name}")
                Timber.e("mc2 ${mediaController.playbackState}")
            }
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {}

    private fun logNotification(notification: StatusBarNotification) {
        val classname = javaClass.simpleName
        BeautyCat.logDivider(classname, ":")
        BeautyCat.logCentered(" ", classname, "New notification")
        BeautyCat.logCentered(":", classname, "packageName: " + notification.packageName)
        BeautyCat.logCentered(":", classname, "id: " + notification.id)
        if (notification.notification.actions != null) {
            BeautyCat.logCentered(" ", classname, "Notification actions")
            for (action in notification.notification.actions) {
                BeautyCat.logCentered(":", classname, "action.title: " + action.title)
            }
        }
        if (notification.notification.extras != null) {
            BeautyCat.logCentered(" ", classname, "Notification extras")
            for (key in notification.notification.extras.keySet()) {
                Timber.d("$key: ${notification.notification.extras[key]}")
            }
        }
        notification.notification.extras.get("android.compactActions")?.let {
            Timber.e("compactActions ${it.javaClass.name}")
        }
        BeautyCat.logDivider(classname, ":")
    }
}

object BeautyCat {

    private const val STYLED_LOG_LENGTH = 48

    private fun tag(tag: String): Timber.Tree {
        return Timber.tag(tag)
    }

    fun logCentered(character: String, tag: String, text: String) {
        val length = text.length + 2
        if (length >= STYLED_LOG_LENGTH) {
            log(tag, "$character%s${text.substring(0, STYLED_LOG_LENGTH - 5)}%s...$character")
        } else {
            val text = "$character%s$text%s${if (length % 2 == 0) "" else " "}$character"
            log(tag, text, repeat(" ", (STYLED_LOG_LENGTH - length) / 2))
        }
    }

    fun logDivider(tag: String, character: String) {
        tag(tag).i(repeat(character, STYLED_LOG_LENGTH))
    }

    private fun log(tag: String, text: String, edge: String = "") {
        tag(tag).i(text, edge, edge)
    }

    private fun repeat(what: String, times: Int): String {
        return TextUtils.join(", ", Collections.nCopies(times, what))
    }
}