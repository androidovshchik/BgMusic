package defpackage.bgmusic

import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class MusicPlayer {

    private lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var rawSoundFactory: ProgressiveMediaSoeeurce.Factory

    fun setup(context: Context) {
        exoPlayer = SimpleExoPlayer.Builder(context)
            .build()
        val httpDataSourceFactory = OkHttpDataSourceFactory(getClient(), null, null)
        val cache = SimpleCache(context.cacheDir, LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100))
        val dataSourceFactory = CacheDataSourceFactory(
            cache, httpDataSourceFactory,
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
        mediaSourceFactory = ExtractorMediaSource.Factory(dataSourceFactory)
    }

    fun playSound(id: Int) {
        exoPlayer.apply {
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