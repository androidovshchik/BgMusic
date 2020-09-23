package defpackage.bgmusic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import defpackage.bgmusic.extension.isOreoPlus
import org.jetbrains.anko.audioManager
import timber.log.Timber
import java.lang.ref.WeakReference

class MusicPlayer(context: Context) : AudioManager.OnAudioFocusChangeListener {

    private val reference = WeakReference(context)

    private var focusRequest: AudioFocusRequest? = null
    private val handler = Handler()
    private val focusLock = Any()
    private var playbackDelayed = false
    private var playbackNowAuthorized = false

    private val player: SimpleExoPlayer

    private val sourceFactory: MediaSourceFactory

    init {
        if (isOreoPlus()) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this, handler)
                .build()
        }
        player = SimpleExoPlayer.Builder(context)
            .build()
        val httpDataSourceFactory = OkHttpDataSourceFactory(getClient(), null, null)
        val cache = SimpleCache(context.cacheDir, LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100))
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)

        cache, httpDataSourceFactory,
        CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
        sourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    fun startPlay() = reference.get()?.let {
        val item =
            MediaItem.fromUri("https://www.oum.ru/upload/audio/554/554915aeb6cf2e9b17ac46dbb1abce01.mp3")
        player.setMediaSource(sourceFactory.createMediaSource(item))
        player.prepare()
        if (isOreoPlus()) {
            val result = it.audioManager.requestAudioFocus(focusRequest!!)
            synchronized(focusLock) {
                playbackNowAuthorized = when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        player.playWhenReady = true
                        true
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }
                    else -> false
                }
            }
        } else {
            val result = it.audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.playWhenReady = true
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    playbackNow()
                }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // ... pausing or ducking depends on your app
            }
        }
    }

    @Suppress("DEPRECATION")
    fun stopPlay() = reference.get()?.run {
        player.playWhenReady = false
        if (audioFocusRequested) {
            audioFocusRequested = false
            val result = if (isOreoPlus()) {
                audioManager.abandonAudioFocusRequest(focusRequest!!)
            } else {
                audioManager.abandonAudioFocus(this)
            }
            Timber.d("Abandon request result is %d", result)
        }
    }

    fun release() {
        stopPlay()
        player.stop()
        player.release()
    }
}