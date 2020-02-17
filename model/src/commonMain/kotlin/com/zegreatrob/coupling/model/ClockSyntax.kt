package com.zegreatrob.coupling.model

import com.soywiz.klock.TimeProvider

interface ClockSyntax {
    val clock: TimeProvider
    fun now() = clock.now()
}
