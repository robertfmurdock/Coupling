package com.zegreatrob.coupling.client.party

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

val httpClient = HttpClient {
    install(ContentNegotiation) { json() }
}

fun createGraphiQLFetcher(url: String, token: String): (graphQlParams: Json) -> Promise<dynamic> = { graphQlParams ->
    MainScope().promise {
        JSON.parse(
            httpClient.post(url) {
                headers { this["Authorization"] = "Bearer $token" }
                setBody(TextContent(JSON.stringify(graphQlParams), ContentType.Application.Json))
            }.body()
        )
    }
}
