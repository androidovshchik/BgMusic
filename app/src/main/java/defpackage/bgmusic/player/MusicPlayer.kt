package defpackage.bgmusic.player

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import defpackage.bgmusic.BuildConfig
import defpackage.bgmusic.extension.isOreoPlus
import defpackage.bgmusic.httpClient
import defpackage.bgmusic.urls
import okhttp3.CacheControl
import org.jetbrains.anko.audioManager
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

interface IHolder {

    fun saveProgress(track: Int)

    fun setAlarmIfNeeded()

    fun cancelAlarm()
}

interface IPlayer : Player.EventListener, AudioManager.OnAudioFocusChangeListener {

    val position: Long

    fun startPlay()

    fun resumePlay()

    fun setMaxVolume()

    fun preparePlaylist(track: Int, progress: Long)

    fun pausePlay()

    fun stopPlay()

    fun release()
}

@SuppressLint("NewApi")
class MusicPlayer(holder: IHolder, context: Context) : IPlayer {

    private val holder = WeakReference(holder)

    private val audioManager = context.audioManager

    @Suppress("DEPRECATION")
    private val focusHandler = Handler()
    private val focusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
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

    private val player = SimpleExoPlayer.Builder(context)
        .build()

    private val sourceFactory: MediaSourceFactory

    private val source = ConcatenatingMediaSource()

    private var isReady = false

    override val position: Long
        get() = player.currentPosition

    init {
        val control = CacheControl.Builder()
            .maxAge(Integer.MAX_VALUE, TimeUnit.SECONDS)
            .build()
        val httpSourceFactory = OkHttpDataSourceFactory(httpClient, null, control)
        val cache = SimpleCache(
            File(context.cacheDir, "exoplayer").apply { mkdirs() },
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(context)
        )
        val cacheSourceFactory = CacheDataSource.Factory()
            .setUpstreamDataSourceFactory(httpSourceFactory)
            .setCache(cache)
        sourceFactory = ProgressiveMediaSource.Factory(cacheSourceFactory)
            .setLoadErrorHandlingPolicy(CustomPolicy())
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(this)
    }

    override fun startPlay() {
        if (isReady) {
            Timber.w("Skipping start playing")
            return
        }
        isReady = true
        holder.get()?.cancelAlarm()
        val result = if (isOreoPlus()) {
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        Timber.d("Request focus: %d", result)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            resumePlay()
        }
    }

    override fun resumePlay() {
        setMaxVolume()
        player.playWhenReady = true
    }

    override fun setMaxVolume() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (BuildConfig.DEBUG) maxVolume / 2 else maxVolume,
            0
        )
    }

    override fun preparePlaylist(track: Int, progress: Long) {
        source.clear()
        urls.forEach {
            source.addMediaSource(sourceFactory.createMediaSource(MediaItem.fromUri(it)))
        }
        player.setMediaSource(source)
        Timber.d("Preparing track=$track progress=$progress")
        player.seekTo(track, progress)
        player.prepare()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Timber.d("Changed focus: %d", focusChange)
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                resumePlay()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                stopPlay()
                holder.get()?.setAlarmIfNeeded()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pausePlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // ... pausing or ducking depends on your app
            }
        }
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        holder.get()?.saveProgress(player.currentWindowIndex)
    }

    override fun pausePlay() {
        player.playWhenReady = false
        holder.get()?.saveProgress(player.currentWindowIndex)
    }

    override fun stopPlay() {
        pausePlay()
        val result = if (isOreoPlus()) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
        Timber.d("Abandon focus: %d", result)
        isReady = false
    }

    override fun release() {
        stopPlay()
        player.stop()
        player.removeListener(this)
        player.release()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Timber.e(error)
    }
}

class CustomPolicy : DefaultLoadErrorHandlingPolicy() {

    override fun getMinimumLoadableRetryCount(dataType: Int) = Int.MAX_VALUE
}