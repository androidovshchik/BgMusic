package defpackage.bgmusic

import android.content.Context
import com.chibatching.kotpref.KotprefModel

class Preferences(context: Context) : KotprefModel(context) {

    override val kotprefName = "${context.packageName}_preferences"

    var track by intPref(0, "track")

    var position by longPref(0, "position")
}