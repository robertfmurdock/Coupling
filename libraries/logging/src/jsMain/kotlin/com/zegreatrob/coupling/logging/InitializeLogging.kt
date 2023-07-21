package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.LOG_LEVEL = if (developmentMode) {
        Level.DEBUG
    } else {
        Level.INFO
    }

    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
}
