package defpackage.bgmusic.service

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import defpackage.bgmusic.BuildConfig
import timber.log.Timber
import java.util.*

class NotificationService : NotificationListenerService() {

    private var has = false

    override fun onNotificationPosted(notification: StatusBarNotification) {
        if (BuildConfig.DEBUG) {
            logNotification(notification)
        }
        Timber.e("is ui ${Looper.myLooper() == Looper.getMainLooper()}")
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

    override fun onNotificationRemoved(notification: StatusBarNotification) {
        Timber.e("R is ui ${Looper.myLooper() == Looper.getMainLooper()}")
    }

    private fun logNotification(notification: StatusBarNotification) {
        BeautyCat.logDivider(tag, ":")
        BeautyCat.logCentered(" ", tag, "New notification")
        BeautyCat.logCentered(":", tag, "id: " + notification.id)
        BeautyCat.logCentered(":", tag, "packageName: " + notification.packageName)
        notification.notification.actions?.let {
            BeautyCat.logCentered(" ", tag, "Notification actions")
            for (action in it) {
                BeautyCat.logCentered(":", tag, "action.title: " + action.title)
            }
        }
        notification.notification.extras?.let {
            BeautyCat.logCentered(" ", tag, "Notification extras")
            for (key in it.keySet()) {
                Timber.d("$key: ${it[key]}")
            }
        }
        BeautyCat.logDivider(tag, ":")
    }

    companion object {

        private val tag = NotificationService::class.java.simpleName
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
            val log = "$character%s$text%s${if (length % 2 == 0) "" else " "}$character"
            log(tag, log, repeat(" ", (STYLED_LOG_LENGTH - length) / 2))
        }
    }

    fun logDivider(tag: String, character: String) {
        tag(tag).i(repeat(character, STYLED_LOG_LENGTH))
    }

    private fun log(tag: String, text: String, edge: String = "") {
        tag(tag).i(text, edge, edge)
    }

    private fun repeat(what: String, times: Int): String {
        return Collections.nCopies(times, what).joinToString(", ")
    }
}