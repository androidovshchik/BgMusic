package defpackage.bgmusic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import defpackage.bgmusic.extension.isOreoPlus
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.jetbrains.anko.audioManager
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class MusicPlayer(context: Context) : AudioManager.OnAudioFocusChangeListener {

    private val reference = WeakReference(context)

    private var focusRequest: AudioFocusRequest? = null
    private val focusHandler = Handler()
    private val focusLock = Any()
    private var isFocusDelayed = false
    private var resumeOnFocusGain = false

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
                .setOnAudioFocusChangeListener(this, focusHandler)
                .build()
        }
        player = SimpleExoPlayer.Builder(context)
            .build()
        val httpSourceFactory = OkHttpDataSourceFactory(
            httpClient, null, CacheControl.Builder()
                .maxAge(Integer.MAX_VALUE, TimeUnit.SECONDS)
                .build()
        )
        val cache = SimpleCache(
            context.cacheDir,
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(context)
        )
        val cacheSourceFactory = CacheDataSource.Factory()
            .setUpstreamDataSourceFactory(httpSourceFactory)
            .setCache(cache)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        sourceFactory = ProgressiveMediaSource.Factory(cacheSourceFactory)
    }

    fun startPlay() = reference.get()?.let {
        val item =
            MediaItem.fromUri("https://www.oum.ru/upload/audio/554/554915aeb6cf2e9b17ac46dbb1abce01.mp3")
        player.setMediaSource(sourceFactory.createMediaSource(item))
        player.prepare()
        val result = if (isOreoPlus()) {
            it.audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            it.audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        Timber.d("Request focus: %d", result)
        when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                player.playWhenReady = true
            }
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                isFocusDelayed = true
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->
                if (isFocusDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        isFocusDelayed = false
                        resumeOnFocusGain = false
                    }
                    player.playWhenReady = true
                }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    isFocusDelayed = false
                }
                stopPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    isFocusDelayed = false
                }
                stopPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // ... pausing or ducking depends on your app
            }
        }
    }

    fun stopPlay() = reference.get()?.let {
        player.playWhenReady = false
        val result = if (isOreoPlus()) {
            it.audioManager.abandonAudioFocusRequest(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            it.audioManager.abandonAudioFocus(this)
        }
        Timber.d("Abandon focus: %d", result)
    }

    fun release() {
        stopPlay()
        player.stop()
        player.release()
    }

    companion object {

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }
}