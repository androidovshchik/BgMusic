package defpackage.bgmusic

import android.annotation.SuppressLint
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import defpackage.bgmusic.extension.pendingActivityFor
import org.jetbrains.anko.notificationManager

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val builder = NotificationCompat.Builder(applicationContext, "main")
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentIntent(pendingActivityFor<MainActivity>())
            .setContentTitle(title ?: getString(R.string.app_name))
            .setContentText(message)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
        notificationManager.notify((99..99999).random(), builder.build())
    }
}