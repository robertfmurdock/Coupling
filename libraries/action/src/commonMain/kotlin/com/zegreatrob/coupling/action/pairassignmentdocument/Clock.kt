package com.zegreatrob.coupling.action.pairassignmentdocument

import kotlin.time.Clock

interface Clock {
    fun currentDate() = Clock.System.now()
}
