package com.zegreatrob.coupling.mongo

import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

interface DbRecordSaveSyntax : DbRecordInfoSyntax {

    suspend fun Json.save(collection: dynamic) = addRecordInfo()
        .run {
            collection.insert(this).unsafeCast<Promise<Unit>>().await()
        }
}

interface DbRecordInfoSyntax : UserEmailSyntax, ClockSyntax {
    fun Json.addRecordInfo() = also {
        this["timestamp"] = now().toDate()
        this["modifiedByUsername"] = userEmail
    }
}