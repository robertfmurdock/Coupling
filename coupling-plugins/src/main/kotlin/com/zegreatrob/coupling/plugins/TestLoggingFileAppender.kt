package com.zegreatrob.coupling.plugins

import java.io.File
import java.time.Instant

object TestLoggingFileAppender {
    fun appendTestmintsLog(logFilePath: String, taskPath: String, message: String) {
        val payload = """
            {"level":"INFO","name":"testmints","message":"$message","taskName":"$taskPath","timestamp":"${Instant.now()}"}
        """.trimIndent()
        val logFile = File(logFilePath)
        logFile.parentFile.mkdirs()
        logFile.appendText(payload + "\n")
    }
}
