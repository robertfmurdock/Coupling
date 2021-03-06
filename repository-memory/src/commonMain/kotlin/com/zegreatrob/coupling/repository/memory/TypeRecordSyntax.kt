package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface TypeRecordSyntax<T> : UserEmailSyntax, ClockSyntax {

    fun T.record() = Record(
        data = this,
        modifyingUserId = userId,
        isDeleted = false,
        timestamp = now()
    )

    fun T.deletionRecord() = Record(
        data = this,
        modifyingUserId = userId,
        isDeleted = true,
        timestamp = now()
    )

}