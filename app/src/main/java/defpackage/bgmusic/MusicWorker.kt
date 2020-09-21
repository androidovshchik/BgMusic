package defpackage.bgmusic

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MusicWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val preferences: Preferences by instance()

    private val server: Server by instance()

    override fun doWork(): Result {
        val header = preferences.authHeader ?: return Result.success()
        if (!applicationContext.exitUnexpected()) {
            try {
                server.logout(header).execute()
                preferences.blockingBulk {
                    logout()
                }
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
        return Result.success()
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