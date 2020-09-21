package defpackage.bgmusic

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class RebootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        with(context) {
            startActivity(intentFor<MainActivity>().newTask())
        }
    }
}