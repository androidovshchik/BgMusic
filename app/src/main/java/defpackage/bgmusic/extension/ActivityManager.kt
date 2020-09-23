@file:Suppress("DEPRECATION")

package defpackage.bgmusic.extension

import android.app.ActivityManager
import android.app.Service

inline fun <reified T : Service> ActivityManager.isRunning(): Boolean {
    for (service in getRunningServices(Int.MAX_VALUE)) {
        if (T::class.java.name == service.service.className) {
            return true
        }
    }
    return false
}

fun ActivityManager.getBaseActivity(packageName: String): String? {
    for (task in getRunningTasks(Int.MAX_VALUE)) {
        task.baseActivity?.let {
            if (it.packageName == packageName) {
                return it.className
            }
        }
    }
    return null
}