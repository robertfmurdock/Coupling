package com.zegreatrob.coupling.sdk.gql

import com.zegreatrob.coupling.json.JsonCouplingQueryResult
import com.zegreatrob.coupling.json.toDomain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface GqlSyntax {
    val performer: QueryPerformer
    suspend fun String.performQuery(): JsonElement = performer.doQuery(this)
    suspend fun performQuery(body: JsonElement): JsonElement = performer.doQuery(body)
    suspend fun JsonElement.perform() = performQuery(this)
        .jsonObject["data"]
        ?.let<JsonElement, JsonCouplingQueryResult>(Json.Default::decodeFromJsonElement)
        ?.toDomain()
    suspend fun String.perform() = performQuery()
        .jsonObject["data"]
        ?.let<JsonElement, JsonCouplingQueryResult>(Json.Default::decodeFromJsonElement)
        ?.toDomain()
}
