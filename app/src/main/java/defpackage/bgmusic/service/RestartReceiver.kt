package defpackage.bgmusic.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import defpackage.bgmusic.playbackChanges
import defpackage.bgmusic.player.ServiceRunnable

class RestartReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        playbackChanges.value = true
        ServiceRunnable(context).run()
    }
}