package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter
import mu.KotlinLoggingConfiguration

fun main() {
    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
    JsonLoggingTestMintsReporter.initialize()
}
