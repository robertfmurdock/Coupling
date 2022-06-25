package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter

fun main() {
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    JsonLoggingTestMintsReporter.initialize()
}
