package defpackage.bgmusic.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import defpackage.bgmusic.BuildConfig
import defpackage.bgmusic.playbackChanges
import defpackage.bgmusic.player.MusicWorker
import defpackage.bgmusic.player.ServiceRunnable

class RestartReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        playbackChanges.value = true
        when (BuildConfig.FLAVOR) {
            "service" -> {
                ServiceRunnable(context).run()
            }
            "worker" -> {
                MusicWorker.cancel(context)
                MusicWorker.launch(context)
            }
        }
    }
}