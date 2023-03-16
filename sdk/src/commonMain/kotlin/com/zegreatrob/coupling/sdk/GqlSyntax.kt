package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toJsonElement
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

interface GqlSyntax {
    val performer: QueryPerformer
    suspend fun String.performQuery(): JsonElement = performer.doQuery(this)
    suspend fun performQuery(body: JsonElement): JsonElement = performer.doQuery(body)
}

interface QueryPerformer {
    suspend fun doQuery(body: String): JsonElement
    suspend fun doQuery(body: JsonElement): JsonElement
    fun postAsync(body: JsonElement): Deferred<JsonElement>

    suspend fun get(path: String): JsonElement
}

suspend inline fun <reified T> GqlSyntax.doQuery(query: String, input: T): JsonElement = performQuery(
    JsonObject(
        mapOf(
            "query" to JsonPrimitive(query),
            "variables" to JsonObject(mapOf("input" to input.toJsonElement())),
        ),
    ),
)

suspend inline fun <reified I, reified O, M> GqlSyntax.doQuery(
    mutation: String,
    input: I,
    resultName: String,
    toOutput: (O) -> M,
): M? = doQuery(mutation, input)
    .jsonObject["data"]!!
    .jsonObject[resultName]
    ?.fromJsonElement<O>()
    ?.let(toOutput)
