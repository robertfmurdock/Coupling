package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.model.Record

interface TypeRecordSyntax<T> : UserEmailSyntax {

    fun T.record() = Record(
        data = this,
        timestamp = DateTime.now(),
        isDeleted = false,
        modifyingUserEmail = userEmail
    )

    fun T.deletionRecord() = Record(
        data = this,
        timestamp = DateTime.now(),
        isDeleted = true,
        modifyingUserEmail = userEmail
    )

}