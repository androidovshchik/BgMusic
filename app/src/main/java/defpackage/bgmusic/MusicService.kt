package defpackage.bgmusic

import android.app.Service
import android.content.Intent
import android.os.IBinder

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service() {

    override fun onCreate() {
        super.onCreate()

    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}