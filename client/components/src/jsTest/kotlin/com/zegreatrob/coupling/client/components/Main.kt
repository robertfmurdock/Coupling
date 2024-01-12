package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.testlogging.JsonLoggingTestMintsReporter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

fun main() {
    val globalRegistratorLib = kotlinext.js.require<dynamic>("@happy-dom/global-registrator")
    val globalRegistrator = globalRegistratorLib.GlobalRegistrator
    console.log("globalRegistrator", globalRegistrator)
    globalRegistrator.register()
    js("global.IS_REACT_ACT_ENVIRONMENT=true")
    js("global.IS_JSDOM=true")
    KotlinLoggingConfiguration.formatter = JsonFormatter
    JsonLoggingTestMintsReporter.initialize()
}
