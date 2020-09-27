package defpackage.bgmusic

import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val urls = arrayOf(
    "https://www.oum.ru/upload/audio/52f/52f961351291b176bca19019a6b3399f.mp3",
    "https://www.oum.ru/upload/audio/554/554915aeb6cf2e9b17ac46dbb1abce01.mp3"
)

val playbackChanges = MutableLiveData<Boolean>()

val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(0, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)
    .build()