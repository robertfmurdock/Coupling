package com.zegreatrob.coupling.server

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger by lazy { KotlinLogging.logger("StartupLogger") }

fun logStartup(port: Int, buildDate: String, gitRevision: String, env: String) {
    logger.info {
        mapOf(
            "message" to "Express server listening",
            "port" to "$port",
            "buildDate" to buildDate,
            "gitRevision" to gitRevision,
            "env" to env,
        )
    }
}
