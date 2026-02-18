package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

fun main() {
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    KotlinLoggingConfiguration.direct.formatter = JsonFormatter
    JsonLoggingTestMintsReporter.initialize()
}
