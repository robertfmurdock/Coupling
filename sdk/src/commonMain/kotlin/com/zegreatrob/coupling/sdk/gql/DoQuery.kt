package com.zegreatrob.coupling.sdk.gql

import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

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
