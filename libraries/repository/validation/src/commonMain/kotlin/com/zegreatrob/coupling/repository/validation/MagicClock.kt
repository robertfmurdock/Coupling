package com.zegreatrob.coupling.repository.validation

import kotlin.time.Clock
import kotlin.time.Instant

class MagicClock : Clock {
    var currentTime: Instant? = null
    override fun now() = currentTime ?: Clock.System.now()
}
