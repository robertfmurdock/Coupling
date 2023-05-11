package com.zegreatrob.coupling.model

import korlibs.time.TimeProvider

interface ClockSyntax {
    val clock: TimeProvider
    fun now() = clock.now()
}
