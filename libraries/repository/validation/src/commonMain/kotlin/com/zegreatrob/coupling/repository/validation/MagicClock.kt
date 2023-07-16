package com.zegreatrob.coupling.repository.validation

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MagicClock : Clock {
    var currentTime: Instant? = null
    override fun now() = currentTime ?: Clock.System.now()
}
