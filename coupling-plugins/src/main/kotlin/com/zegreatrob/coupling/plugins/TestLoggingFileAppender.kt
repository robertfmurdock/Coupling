package com.zegreatrob.coupling.plugins

import java.io.File
import java.time.Instant

object TestLoggingFileAppender {
    fun appendTestmintsLog(logFilePath: String, taskPath: String, runId: String, message: String) {
        val payload = """
            {"type":"Log","platform":"js","run_id":"$runId","task":"$taskPath","logger":"testmints","message":"$message","timestamp":"${Instant.now()}"}
        """.trimIndent()
        val logFile = File(logFilePath)
        logFile.parentFile.mkdirs()
        logFile.appendText(payload + "\n")
    }
}
