package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserIdSyntax

interface RecordSyntax : UserIdSyntax, ClockSyntax {
    fun <T> T.toRecord() = Record(this, userId, false, now())
}