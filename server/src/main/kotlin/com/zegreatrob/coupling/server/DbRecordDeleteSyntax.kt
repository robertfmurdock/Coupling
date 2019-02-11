package com.zegreatrob.coupling.server

import kotlin.js.Json

interface DbRecordDeleteSyntax : DbRecordLoadSyntax, DbRecordSaveSyntax {
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
                    .let { it.save(collection) }

    private fun Json.addIsDeleted() = also { this["isDeleted"] = true }

}