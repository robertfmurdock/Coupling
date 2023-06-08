package com.zegreatrob.coupling.server.action.pairassignmentdocument

import korlibs.time.DateTime

interface Clock {
    fun currentDate() = DateTime.now()
}
