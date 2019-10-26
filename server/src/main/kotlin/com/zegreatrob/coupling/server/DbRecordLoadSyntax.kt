package com.zegreatrob.coupling.server

import com.soywiz.klock.internal.toDateTime
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import kotlinx.coroutines.await
import kotlin.js.*

interface DbRecordLoadSyntax : JsonTimestampSyntax {

    suspend fun findByQuery(query: Json, collection: dynamic, idProperty: String = "id") = rawFindBy(query, collection)
        .await()
        .findLatestOfEachRecord(idProperty)
        .filter { it["isDeleted"] != true }

    suspend fun findDeletedByQuery(tribeId: TribeId, collection: dynamic, idProperty: String = "id") = rawFindBy(
        json("tribe" to tribeId.value), collection
    )
        .await()
        .findLatestOfEachRecord(idProperty)
        .filter { it["isDeleted"] == true }

    fun rawFindBy(query: Json, collection: dynamic) = collection.find(query).unsafeCast<Promise<Array<Json>>>()

    private fun Array<Json>.dbJsonArrayLoad() = findLatestOfEachRecord()
        .filter { it["isDeleted"] != true }

    private fun Array<Json>.findLatestOfEachRecord(idProperty: String = "id") = map { it.applyIdCorrection(idProperty) }
        .groupBy { it["_id"].toString() }
        .map { it.value.latestByTimestamp() }
        .filterNotNull()

    fun Json.applyIdCorrection(idProperty: String = "id") = also {
        this["_id"] = this[idProperty].unsafeCast<String?>() ?: this["_id"]
    }

    fun List<Json>.latestByTimestamp() = sortedByDescending { it.timeStamp() }.firstOrNull()

    suspend fun getLatestRecordWithId(id: String, collection: dynamic, usesRawId: Boolean = true) =
        getAllRecordsWithId(id, collection, usesRawId)
            .latestByTimestamp()

    private suspend fun getAllRecordsWithId(id: String, collection: dynamic, usesRawId: Boolean = true) = listOf(
        rawFindBy(json("id" to id), collection),
        if (usesRawId) rawFindBy(json("_id" to id), collection) else Promise.resolve(emptyArray())
    )
        .map { it.await().toList() }
        .flatten()
}

interface JsonTimestampSyntax {
    fun Json.timeStamp() = this["timestamp"]?.unsafeCast<Date>()?.toDateTime()
}
