package com.zegreatrob.coupling.components

fun main() {
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    JsonLoggingTestMintsReporter.initialize()
}
