package defpackage.bgmusic

import android.os.Bundle
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.PatternFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.util.*

class LogInterceptor : HttpLoggingInterceptor.Logger {

    override fun log(message: String) {
        Timber.tag("REST").d(message)
    }
}

class LogTree(logDir: File) : Timber.DebugTree() {

    init {
        if (BuildConfig.SAVE_LOGS) {
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val config = LogConfiguration.Builder()
                .t()
                .build()
            val filePrinter = FilePrinter.Builder(logDir.path)
                .fileNameGenerator(DateFileNameGenerator())
                .backupStrategy(NeverBackupStrategy())
                .flattener(PatternFlattener("{d yyyy-MM-dd HH:mm:ss.SSS} {l}: {m}"))
                .build()
            XLog.init(config, filePrinter)
        }
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return "${super.createStackElementTag(element)}:${element.methodName}:${element.lineNumber}"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        if (BuildConfig.SAVE_LOGS) {
            XLog.log(priority, "$tag: $message", t)
        }
    }
}

object BeautyCat {

    private const val STYLED_LOG_LENGTH = 48

    private fun tag(tag: String): Timber.Tree {
        return Timber.tag(tag)
    }

    fun log(tag: String, text: String, char: String = " ") {
        val length = text.length + 2
        if (length >= STYLED_LOG_LENGTH) {
            print(tag, "$char%s${text.substring(0, STYLED_LOG_LENGTH - 5)}%s...$char")
        } else {
            val log = "$char%s$text%s${if (length % 2 == 0) "" else " "}$char"
            print(tag, log, repeat(" ", (STYLED_LOG_LENGTH - length) / 2))
        }
    }

    fun map(tag: String, extras: Bundle, char: String) {
        for (key in extras.keySet()) {
            log(tag, "$key: ${extras[key]}", char)
        }
    }

    fun div(tag: String, char: String) {
        tag(tag).i(repeat(char, STYLED_LOG_LENGTH))
    }

    private fun print(tag: String, text: String, edge: String = "") {
        tag(tag).i(text, edge, edge)
    }

    private fun repeat(what: String, times: Int): String {
        return Collections.nCopies(times, what).joinToString("")
    }
}