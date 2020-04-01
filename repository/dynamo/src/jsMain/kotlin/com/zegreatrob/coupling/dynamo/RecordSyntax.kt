package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface RecordSyntax : UserEmailSyntax, ClockSyntax {
    fun <T> T.toRecord() = Record(this, userEmail, false, now())
}