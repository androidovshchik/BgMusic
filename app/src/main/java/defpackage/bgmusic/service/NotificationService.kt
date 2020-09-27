package defpackage.bgmusic.service

import android.annotation.SuppressLint
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.UiThread
import defpackage.bgmusic.BuildConfig
import defpackage.bgmusic.playbackChanges
import timber.log.Timber
import java.util.*

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

object BeautyCat {

    private const val STYLED_LOG_LENGTH = 48

    private fun tag(tag: String): Timber.Tree {
        return Timber.tag(tag)
    }

    fun log(tag: String, text: String, char: String = " ") {
        val length = text.length + 2
        if (length >= STYLED_LOG_LENGTH) {
            print(tag, "$char%s${text.substring(0, STYLED_LOG_LENGTH - 5)}%s...$char")
        } else {
            val log = "$char%s$text%s${if (length % 2 == 0) "" else " "}$char"
            print(tag, log, repeat(" ", (STYLED_LOG_LENGTH - length) / 2))
        }
    }

    fun map(tag: String, extras: Bundle, char: String) {
        for (key in extras.keySet()) {
            log(tag, "$key: ${extras[key]}", char)
        }
    }

    fun div(tag: String, char: String) {
        tag(tag).i(repeat(char, STYLED_LOG_LENGTH))
    }

    private fun print(tag: String, text: String, edge: String = "") {
        tag(tag).i(text, edge, edge)
    }

    private fun repeat(what: String, times: Int): String {
        return Collections.nCopies(times, what).joinToString("")
    }
}