package defpackage.bgmusic

import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val playbackChanges = MutableLiveData<Boolean>()

val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(0, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)
    .build()