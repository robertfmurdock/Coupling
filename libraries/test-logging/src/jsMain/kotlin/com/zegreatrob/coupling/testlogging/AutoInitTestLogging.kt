package com.zegreatrob.coupling.testlogging

import com.zegreatrob.coupling.logging.initializeLogging
import io.github.oshai.kotlinlogging.KotlinLogging

@Suppress("unused")
private val autoInitTestLogging = run {
    initializeLogging(true)
    JsonLoggingTestMintsReporter.initialize()
    KotlinLogging.logger("testmints").info { "js-test-init" }
}
