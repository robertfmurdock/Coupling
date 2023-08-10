package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.logLevel = if (developmentMode) {
        Level.DEBUG
    } else {
        Level.INFO
    }

    KotlinLoggingConfiguration.formatter = JsonFormatter
}
