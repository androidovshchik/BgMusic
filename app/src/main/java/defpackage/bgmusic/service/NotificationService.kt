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
        val char = chars[chars.indices.random()]
        BeautyCat.div(tag, char)
        BeautyCat.log(tag, " ", "New notification")
        BeautyCat.log(tag, char, "id: " + notification.id)
        BeautyCat.log(tag, char, "packageName: " + notification.packageName)
        notification.notification.actions?.let {
            BeautyCat.log(tag, " ", "Notification actions")
            for (action in it) {
                BeautyCat.log(tag, char, "action.title: ${action.title}")
            }
        }
        notification.notification.extras?.let {
            BeautyCat.log(tag, " ", "Notification extras")
            for (key in it.keySet()) {
                BeautyCat.log(tag, char, "$key: ${it[key]}")
            }
        }
        BeautyCat.div(tag, char)
    }

    companion object {

        private val tag = NotificationService::class.java.simpleName

        private val chars = arrayOf("*", ":", ";", "$", "#", "@", "&", "%", "=", "~", "-")
    }
}

object BeautyCat {

    private const val STYLED_LOG_LENGTH = 48

    private fun tag(tag: String): Timber.Tree {
        return Timber.tag(tag)
    }

    fun log(tag: String, character: String, text: String) {
        val length = text.length + 2
        if (length >= STYLED_LOG_LENGTH) {
            print(tag, "$character%s${text.substring(0, STYLED_LOG_LENGTH - 5)}%s...$character")
        } else {
            val log = "$character%s$text%s${if (length % 2 == 0) "" else " "}$character"
            print(tag, log, repeat(" ", (STYLED_LOG_LENGTH - length) / 2))
        }
    }

    fun extra() {

    }

    fun div(tag: String, character: String) {
        tag(tag).i(repeat(character, STYLED_LOG_LENGTH))
    }

    private fun print(tag: String, text: String, edge: String = "") {
        tag(tag).i(text, edge, edge)
    }

    private fun repeat(what: String, times: Int): String {
        return Collections.nCopies(times, what).joinToString(", ")
    }
}