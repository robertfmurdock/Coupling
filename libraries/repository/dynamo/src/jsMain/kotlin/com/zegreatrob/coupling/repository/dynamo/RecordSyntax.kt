package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserIdProvider

interface RecordSyntax :
    UserIdProvider,
    ClockProvider {
    fun <T> T.toRecord() = Record(this, userId.value, false, now())
}
