package com.zegreatrob.coupling.sdk

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

interface KtorQueryPerformer : QueryPerformer, KtorSyntax {

    override suspend fun doQuery(body: Json): Json {
        return postStringToJsonObject(body)
    }

    override suspend fun doQuery(body: String): Json {
        return postStringToJsonObject(json("query" to body))
    }

    override fun postAsync(body: dynamic) = MainScope().async {
        postStringToJsonObject(body)
    }

    private suspend fun postStringToJsonObject(body: dynamic): Json {
        val result = JSON.parse<Json>(client.post("/api/graphql") {
            header("Authorization", "Bearer ${getIdToken()}")
            this.body = TextContent(JSON.stringify(body), ContentType.Application.Json)
        })
        val errors = result["errors"]
        if (errors != null && errors.unsafeCast<Array<Any?>>().isNotEmpty()) {
            throw Error("Failed with errors: ${JSON.stringify(errors)}. Full body is ${JSON.stringify(result)}")
        }
        return result
    }

    override suspend fun get(path: String): String? = client.get(path) {
        header("Authorization", "Bearer ${getIdToken()}")
    }
}
