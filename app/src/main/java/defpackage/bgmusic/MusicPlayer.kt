package defpackage.bgmusic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import defpackage.bgmusic.extension.isOreoPlus
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.jetbrains.anko.audioManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

private val urls = arrayOf(
    "https://www.oum.ru/upload/audio/52f/52f961351291b176bca19019a6b3399f.mp3",
    "https://www.oum.ru/upload/audio/554/554915aeb6cf2e9b17ac46dbb1abce01.mp3"
)

@Suppress("MemberVisibilityCanBePrivate")
class MusicPlayer(context: Context) : AudioManager.OnAudioFocusChangeListener {

    private val audioManager = context.audioManager
    private var focusRequest: AudioFocusRequest? = null
    private val focusHandler = Handler()
    private val focusLock = Any()
    private var isFocusDelayed = false
    private var resumeOnFocusGain = false

    private val player: ExoPlayer

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
        player.setPo
        player.repeatMode = Player.REPEAT_MODE_ALL
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
        sourceFactory = ProgressiveMediaSource.Factory(cacheSourceFactory)
    }

    fun startPlay() {
        val source = ConcatenatingMediaSource()
        source.addMediaSources(urls.map { url ->
            sourceFactory.createMediaSource(MediaItem.fromUri(url))
        })
        player.setMediaSource(source)
        player.prepare()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        val result = if (isOreoPlus()) {
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
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
        Timber.d("Changed focus: %d", focusChange)
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
                player.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    isFocusDelayed = false
                }
                player.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // ... pausing or ducking depends on your app
            }
        }
    }

    fun stopPlay() {
        player.playWhenReady = false
        val result = if (isOreoPlus()) {
            audioManager.abandonAudioFocusRequest(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
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

class CustomPolicy : DefaultLoadErrorHandlingPolicy() {

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        // Replace NoConnectivityException with the corresponding
        // exception for the used DataSource.
        return if (loadErrorInfo.exception is NoConnectivityException) {
            5000 // Retry every 5 seconds.
        } else {
            C.TIME_UNSET // Anything else is surfaced.
        }
    }

    override fun getMinimumLoadableRetryCount(dataType: Int) = Int.MAX_VALUE
}