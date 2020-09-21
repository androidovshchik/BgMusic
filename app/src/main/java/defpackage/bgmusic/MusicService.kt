package defpackage.bgmusic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer

@Suppress("MemberVisibilityCanBePrivate")
class MusicService : Service() {

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var rawSoundFactory: ProgressiveMediaSource.Factory

    private val binder = Binder()

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(applicationContext)
        rawSoundFactory = ProgressiveMediaSource.Factory(DataSource.Factory {
            RawResourceDataSource(applicationContext)
        })
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onUnbind(intent: Intent): Boolean {
        stopPlay()
        return true
    }

    fun playSound(id: Int) {
        exoPlayer.apply {
            prepare(
                rawSoundFactory.createMediaSource(
                    RawResourceDataSource.buildRawResourceUri(id)
                )
            )
            playWhenReady = true
        }
    }

    fun stopPlay() {
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        stopPlay()
        exoPlayer.apply {
            stop()
            release()
        }
        super.onDestroy()
    }
}