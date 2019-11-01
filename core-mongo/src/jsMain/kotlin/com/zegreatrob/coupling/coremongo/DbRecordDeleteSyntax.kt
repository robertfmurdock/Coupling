package com.zegreatrob.coupling.coremongo

import kotlin.js.Json

interface DbRecordDeleteSyntax : DbRecordLoadSyntax, DbRecordSaveSyntax {
    suspend fun <T> deleteEntity(
        id: String,
        collection: dynamic,
        entityName: String,
        toDomain: Json.() -> T?,
        toDbJson: T.() -> Json,
        usesRawId: Boolean = true
    ): Boolean = getLatestRecordWithId(id, collection, usesRawId)
        ?.toDomain()
        ?.toDbJson()
        ?.addIsDeleted()
        ?.let { it.save(collection); true }
        ?: false

    private fun Json.addIsDeleted() = also { this["isDeleted"] = true }

}