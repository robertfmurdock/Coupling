package com.zegreatrob.coupling.sdk

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray

interface KtorQueryPerformer : QueryPerformer, KtorSyntax {

    override suspend fun doQuery(body: JsonElement): JsonElement {
        return postStringToJsonObject(body)
    }

    override suspend fun doQuery(body: String) =
        postStringToJsonObject(
            JsonObject(mapOf("query" to JsonPrimitive(body))),
        )

    override fun postAsync(body: JsonElement) = MainScope().async {
        postStringToJsonObject(body)
    }

    private suspend fun postStringToJsonObject(body: JsonElement): JsonElement {
        val result = client.post("api/graphql") {
            header("Authorization", "Bearer ${getIdToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<JsonObject>()
        val errors = result["errors"]
        if (errors != null && errors.jsonArray.isNotEmpty()) {
            throw Error("Failed with errors: $errors. Full body is $result")
        }
        return result
    }

    override suspend fun get(path: String): JsonElement = client.get(path) {
        header("Authorization", "Bearer ${getIdToken()}")
    }.body()
}
