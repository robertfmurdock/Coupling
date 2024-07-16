package com.zegreatrob.coupling.action.pairassignmentdocument

interface Clock {
    fun currentDate() = kotlinx.datetime.Clock.System.now()
}
