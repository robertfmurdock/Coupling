package com.zegreatrob.coupling.server.action.pairassignmentdocument

interface Clock {
    fun currentDate() = kotlinx.datetime.Clock.System.now()
}
