package com.zegreatrob.coupling.mongo

import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlin.js.Json

interface DbRecordInfoSyntax : UserEmailSyntax,
    ClockSyntax {
    fun Json.addRecordInfo() = also {
        this["timestamp"] = now().toDate()
        this["modifiedByUsername"] = userEmail
    }
}