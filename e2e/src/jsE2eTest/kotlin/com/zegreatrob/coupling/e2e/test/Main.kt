package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.logging.JsonFormatter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

fun main() {
    KotlinLoggingConfiguration.direct.logLevel = Level.INFO
    KotlinLoggingConfiguration.direct.formatter = JsonFormatter
    E2eJsonlTestMintsReporter.initialize()
}
