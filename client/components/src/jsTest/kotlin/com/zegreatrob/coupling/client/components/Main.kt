package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import kotlinx.browser.window

fun main() {
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    window.asDynamic().matchMedia = { query: String -> nonMatchingMediaQuery(query) }
    KotlinLoggingConfiguration.direct.formatter = JsonFormatter
    JsonLoggingTestMintsReporter.initialize()
}

internal fun nonMatchingMediaQuery(query: String): dynamic {
    val media = js("Object.assign(new window.EventTarget(), {matches: false})")
    media.media = query
    return media
}
