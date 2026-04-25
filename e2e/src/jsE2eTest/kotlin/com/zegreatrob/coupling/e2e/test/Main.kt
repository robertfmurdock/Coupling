package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.logging.JsonFormatter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

fun main() {
    KotlinLoggingConfiguration.direct.formatter = JsonFormatter
    E2eJsonlTestMintsReporter.initialize()
}
