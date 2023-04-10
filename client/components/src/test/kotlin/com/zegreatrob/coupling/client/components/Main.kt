package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter

fun main() {
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    JsonLoggingTestMintsReporter.initialize()
}
