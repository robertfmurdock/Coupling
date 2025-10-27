package com.zegreatrob.coupling.sdk.gql

import com.zegreatrob.coupling.json.toJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend inline fun <reified T> GqlTrait.doQuery(query: String, input: T): JsonElement = performQuery(
    JsonObject(
        mapOf(
            "query" to JsonPrimitive(query),
            "variables" to JsonObject(mapOf("input" to input.toJsonElement())),
        ),
    ),
)
