package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.time.Instant

object TestLoggingFileAppender {
    private val mapper = ObjectMapper()

    fun appendTestmintsLog(logFilePath: String, taskPath: String, runId: String, message: String) {
        appendEvent(
            logFilePath,
            mapOf(
                "type" to "Log",
                "platform" to "js",
                "run_id" to runId,
                "task" to taskPath,
                "logger" to "testmints",
                "message" to message,
                "timestamp" to Instant.now().toString(),
            )
        )
    }

    @Synchronized
    fun appendEvent(logFilePath: String, event: Map<String, Any?>) {
        val logFile = File(logFilePath)
        logFile.parentFile.mkdirs()
        logFile.appendText(mapper.writeValueAsString(event) + "\n")
    }
}
