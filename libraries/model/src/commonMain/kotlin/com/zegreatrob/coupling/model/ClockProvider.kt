package com.zegreatrob.coupling.model

import kotlin.time.Clock

interface ClockProvider {
    val clock: Clock
    fun now() = clock.now()
}
