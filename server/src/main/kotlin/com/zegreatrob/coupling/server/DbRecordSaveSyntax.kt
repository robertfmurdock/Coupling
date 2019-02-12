package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.UserEmailSyntax
import kotlinx.coroutines.await
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise

interface DbRecordSaveSyntax : DbRecordInfoSyntax {

    suspend fun Json.save(collection: dynamic) = addRecordInfo()
            .run {
                collection.insert(this).unsafeCast<Promise<Unit>>().await()
            }
}

interface DbRecordInfoSyntax : UserEmailSyntax {
    fun Json.addRecordInfo() = also {
        this["timestamp"] = Date()
        this["modifiedByUsername"] = userEmail
    }
}