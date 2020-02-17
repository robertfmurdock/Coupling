package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface TypeRecordSyntax<T> : UserEmailSyntax, ClockSyntax {

    fun T.record() = Record(
        data = this,
        timestamp = now(),
        isDeleted = false,
        modifyingUserEmail = userEmail
    )

    fun T.deletionRecord() = Record(
        data = this,
        timestamp = now(),
        isDeleted = true,
        modifyingUserEmail = userEmail
    )

}