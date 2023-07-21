package com.zegreatrob.coupling.testlogging

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import io.github.oshai.kotlinlogging.KotlinLogging

class JsonLoggingTestMintsReporter private constructor() : MintReporter {

    private val logger by lazy { KotlinLogging.logger("testmints") }
    override fun exerciseStart(context: Any) = logger.info {
        mapOf(
            "message" to "exerciseStart",
            "context" to context.toString(),
        )
    }

    override fun exerciseFinish() = logger.info { "exerciseFinish" }
    override fun verifyStart(payload: Any?) = logger.info {
        mapOf(
            "message" to "verifyStart",
            "payload" to payload.toString(),
        )
    }

    override fun verifyFinish() = logger.info { "verifyFinish" }

    companion object {
        fun initialize() {
            MintReporterConfig.reporter = JsonLoggingTestMintsReporter()
        }
    }
}
