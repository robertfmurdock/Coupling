package com.zegreatrob.coupling.client.party

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

val httpClient = HttpClient {
    install(ContentNegotiation) { json() }
}

fun createGraphiQLFetcher(url: String, token: String): (graphQlParams: Json) -> Promise<dynamic> = { graphQlParams ->
    MainScope().promise {
        JSON.parse(httpClient.post(url) {
            headers { this["Authorization"] = "Bearer $token" }
            setBody(TextContent(JSON.stringify(graphQlParams), ContentType.Application.Json))
        }.body())
    }
}
