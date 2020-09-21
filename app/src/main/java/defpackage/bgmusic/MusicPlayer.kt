package defpackage.bgmusic

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import timber.log.Timber

class MusicPlayer {

    private var audioFocusRequest: AudioFocusRequest? = null
    private val audioFocusChangeListener = { focus: Int ->
        Timber.d("OnAudioFocusChange %d", focus)
        if (focus < AudioManager.AUDIOFOCUS_NONE) {
            Timber.w("No audio focus")
            stopPlay()
            abandonFocus()
        }
    }

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var rawSoundFactory: ProgressiveMediaSoeeurce.Factory

    fun setup(context: Context) {
        exoPlayer = SimpleExoPlayer.Builder(context)
            .build()
        val httpDataSourceFactory = OkHttpDataSourceFactory(getClient(), null, null)
        val cache = SimpleCache(context.cacheDir, LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100))
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)

        cache, httpDataSourceFactory,
        CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
        mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    fun playSound(id: Int) {
        exoPlayer.apply {
            playWhenReady = true
        }
    }

    fun stopPlay() {
        exoPlayer.playWhenReady = false
    }

    @Suppress("DEPRECATION")
    private fun abandonFocus() {
        if (audioFocusRequested) {
            audioFocusRequested = false
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager().abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                audioManager().abandonAudioFocus(audioFocusChangeListener)
            }
            Timber.d("Abandon request result is %d", result)
        }
    }

    override fun onDestroy() {
        stopPlay()
        exoPlayer.stop()
        exoPlayer.release()
    }
}