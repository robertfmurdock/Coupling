package com.zegreatrob.coupling.model

import kotlinx.datetime.Clock

interface ClockProvider {
    val clock: Clock
    fun now() = clock.now()
}
