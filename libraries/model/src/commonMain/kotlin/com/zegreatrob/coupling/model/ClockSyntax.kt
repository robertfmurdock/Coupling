package com.zegreatrob.coupling.model

import kotlinx.datetime.Clock

interface ClockSyntax {
    val clock: Clock
    fun now() = clock.now()
}
