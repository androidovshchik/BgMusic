package defpackage.bgmusic

import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val urls = arrayOf(
    "https://cdn6.sefon.pro/files/prev/1/Юрий Никулин - А Нам Все Равно (192kbps).mp3",
    "https://cdn3.sefon.pro/files/prev/177/Юрий Никулин - Постой, паровоз! (192kbps).mp3",
    "https://cdn6.sefon.pro/files/prev/177/Юрий Никулин - Если б я был султан (192kbps).mp3"
)

val playbackChanges = MutableLiveData<Boolean>()

val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(0, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)
    .build()