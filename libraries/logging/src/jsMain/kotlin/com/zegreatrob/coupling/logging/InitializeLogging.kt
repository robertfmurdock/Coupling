package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.direct.logLevel = if (developmentMode) {
        Level.DEBUG
    } else {
        Level.INFO
    }

    KotlinLoggingConfiguration.direct.formatter = JsonFormatter
}
