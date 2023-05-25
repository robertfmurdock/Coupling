package com.zegreatrob.coupling.logging

import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.LOG_LEVEL = if (developmentMode) {
        KotlinLoggingLevel.DEBUG
    } else {
        KotlinLoggingLevel.INFO
    }

    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
}
