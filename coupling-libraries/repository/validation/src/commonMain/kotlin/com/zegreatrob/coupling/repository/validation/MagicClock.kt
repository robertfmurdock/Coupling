package com.zegreatrob.coupling.repository.validation

import korlibs.time.DateTime
import korlibs.time.TimeProvider

class MagicClock : TimeProvider {
    var currentTime: DateTime? = null
    override fun now() = currentTime ?: TimeProvider.now()
}
