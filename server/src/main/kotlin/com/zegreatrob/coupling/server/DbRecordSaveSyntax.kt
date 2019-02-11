package com.zegreatrob.coupling.server

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

interface DbRecordInfoSyntax : UserContextSyntax {
    fun Json.addRecordInfo() = also {
        this["timestamp"] = Date()
        this["modifiedByUsername"] = username()
    }
}