package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider

class MagicClock : TimeProvider {
    var currentTime: DateTime? = null
    override fun now() = currentTime ?: TimeProvider.now()
}
