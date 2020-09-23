package defpackage.bgmusic

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class MusicWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return Result.success()
    }

    companion object {

        private const val NAME = "Music"

        fun launch(context: Context) {
            val request = OneTimeWorkRequestBuilder<MusicWorker>()
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).apply {
                enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
            }
        }
    }
}