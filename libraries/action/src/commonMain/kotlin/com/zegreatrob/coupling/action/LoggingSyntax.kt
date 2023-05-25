package com.zegreatrob.coupling.action

import mu.KotlinLogging

private val theLogger by lazy { KotlinLogging.logger("ActionLogger") }

interface LoggingSyntax {

    val logger get() = theLogger
}
