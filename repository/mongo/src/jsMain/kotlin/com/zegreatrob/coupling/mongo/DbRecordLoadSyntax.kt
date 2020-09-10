package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.tribe.TribeId
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

    fun List<Json>.latestByTimestamp() = maxByOrNull { it.timeStamp() }

    suspend fun getLatestRecordWithId(id: String, collection: dynamic, usesRawId: Boolean = true) =
        getAllRecordsWithId(id, collection, usesRawId)
            .latestByTimestamp()

    private suspend fun getAllRecordsWithId(id: String, collection: dynamic, usesRawId: Boolean = true) = listOf(
        rawFindBy(json("id" to id), collection),
        if (id.isValidObjectId() && usesRawId)
            safeRawFindByMongoId(id, collection)
        else
            Promise.resolve(emptyArray())
    )
        .map { it.await().toList() }
        .flatten()

    private fun safeRawFindByMongoId(id: String, collection: dynamic): Promise<Array<Json>> = try {
        rawFindBy(json("_id" to id), collection)
    } catch (badId: Throwable) {
        Promise.resolve(emptyArray())
    }
}

interface JsonTimestampSyntax {
    fun Json.timeStamp() = this["timestamp"]?.unsafeCast<Date>()?.toDateTime() ?: DateTime.EPOCH
}

fun String.isValidObjectId(): Boolean {
    val objectIdType = js("require('bson').ObjectID")
    val valid = objectIdType.isValid(this)
    return valid.unsafeCast<Boolean>()
}
