package defpackage.bgmusic

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class MusicWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

    }

    companion object {

        private const val NAME = "MIDNIGHT"

        fun launch(context: Context) {
            val now = DateTime.now()
            val delay = Duration(now, now.plusDays(1).withTime(0, 0, 0, 0)).millis
            val request = OneTimeWorkRequestBuilder<MidnightWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).apply {
                enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
            }
        }
    }
}