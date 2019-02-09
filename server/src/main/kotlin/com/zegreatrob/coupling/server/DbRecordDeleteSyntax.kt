package com.zegreatrob.coupling.server

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

interface DbRecordDeleteSyntax : DbRecordLoadSyntax {
    suspend fun <T> deleteEntity(
            id: String,
            collection: dynamic,
            entityName: String,
            toDomain: Json.() -> T?,
            toDbJson: T.() -> Json
    ) =
            getLatestRecordWithId(id, collection)
                    ?.toDomain()
                    .let {
                        it ?: throw Exception(message = "$entityName could not be deleted because they do not exist.")
                    }
                    .toDbJson()
                    .addIsDeleted()
                    .insertRecord(collection)

    private suspend fun Json.insertRecord(collection: dynamic) =
            collection.insert(this).unsafeCast<Promise<Unit>>().await()

    private fun Json.addIsDeleted() = also { this["isDeleted"] = true }

}