package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlin.js.json

interface DynamoRecordJsonMapping : DynamoDatatypeSyntax, UserEmailSyntax {

    fun recordJson() = json(
        "timestamp" to DateTime.now().isoWithMillis(),
        "modifyingUserEmail" to userEmail
    )

}