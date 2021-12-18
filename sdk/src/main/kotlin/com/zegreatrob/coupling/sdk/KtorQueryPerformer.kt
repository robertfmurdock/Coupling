package com.zegreatrob.coupling.sdk

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

interface KtorQueryPerformer : QueryPerformer, KtorSyntax {

    override suspend fun doQuery(body: Json): dynamic {
        return postStringToJsonObject(body)
    }

    override suspend fun doQuery(body: String): dynamic {
        return postStringToJsonObject(json("query" to body))
    }

    override fun postAsync(body: dynamic) = MainScope().async {
        postStringToJsonObject(body)
    }

    private suspend fun postStringToJsonObject(body: dynamic) = JSON.parse<Json>(client.post("/api/graphql") {
        this.body = TextContent(JSON.stringify(body), ContentType.Application.Json)
    })
}
