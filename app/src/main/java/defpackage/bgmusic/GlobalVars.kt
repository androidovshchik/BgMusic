package defpackage.bgmusic

import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

val urls = arrayOf(
    "http://sfxcontent.s3.amazonaws.com/soundfx/DrumRoll-Snare1.mp3",
    "http://cs.klan-hub.ru/zombie/sound/ambience/rire_diabolique4.wav",
    "http://www.motorweb-es.com/media/FERRARI1.WAV",
    "http://cs.klan-hub.ru/zombie/sound/zombie_plague/survivor2.wav",
    "http://www.simphonics.com/library/WaveFiles/SimPhonics Tones Collection/Generic Tones/Telephone/ring.wav",
    "http://psychomax.fobby.net/Heaven_Smile2.wav",
    "http://www.flagislandwebcam.com/animalsounds/cricket1.wav",
    "http://www.planetarypinball.com/mm5/Williams/games/arabian/Sounds/laugh.wav",
    //"https://cdn6.sefon.pro/files/prev/1/Юрий Никулин - А Нам Все Равно (192kbps).mp3",
    //"https://cdn3.sefon.pro/files/prev/177/Юрий Никулин - Постой, паровоз! (192kbps).mp3",
    //"https://cdn6.sefon.pro/files/prev/177/Юрий Никулин - Если б я был султан (192kbps).mp3"
)

val playbackChanges = MutableLiveData<Boolean>()

val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(0, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)
    .addInterceptor(
        HttpLoggingInterceptor(LogInterceptor())
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
    )
    .build()