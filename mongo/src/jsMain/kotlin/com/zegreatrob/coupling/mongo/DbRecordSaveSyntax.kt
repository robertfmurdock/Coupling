package com.zegreatrob.coupling.mongo

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

interface DbRecordSaveSyntax : DbRecordInfoSyntax {

    suspend fun Json.save(collection: dynamic) = addRecordInfo()
        .run {
            collection.insert(this).unsafeCast<Promise<Unit>>().await()
        }
}
