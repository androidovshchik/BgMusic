package defpackage.bgmusic.player

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.observeForeverFreshly
import androidx.lifecycle.removeFreshObserver
import androidx.work.*
import defpackage.bgmusic.playbackChanges
import java.util.concurrent.TimeUnit

class MusicWorker(context: Context, params: WorkerParameters) : Worker(context, params),
    Observer<Boolean> {

    private val holder by lazy { ContextHolder(applicationContext) }

    private val player by lazy { MusicPlayer(holder, applicationContext) }

    override fun doWork(): Result {
        try {
            player.preparePlaylist(preferences.track, preferences.position)
            player.startPlay()
            playbackChanges.observeForeverFreshly(this)
            Thread.sleep(TimeUnit.MINUTES.toMillis(10))
        } catch (e: Throwable) {
            if (!isStopped) {
                onStopped()
            }
        }
        return Result.success()
    }

    override fun onChanged(hasPause: Boolean) {
        if (hasPause) {
            player.startPlay()
        }
    }

    override fun onStopped() {
        holder.saveProgress()
        playbackChanges.removeFreshObserver(this)
        player.release()
    }

    companion object {

        private const val NAME = "Music"

        fun launch(context: Context) {
            val request = OneTimeWorkRequestBuilder<MusicWorker>()
                .build()
            WorkManager.getInstance(context).apply {
                enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
            }
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(NAME)
            }
        }
    }
}