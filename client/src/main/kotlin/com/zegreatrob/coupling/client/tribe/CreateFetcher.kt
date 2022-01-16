package com.zegreatrob.coupling.client.tribe

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

val httpClient = HttpClient {
    install(JsonFeature)
}

fun createGraphiQLFetcher(url: String, token: String): (graphQlParams: Json) -> Promise<dynamic> = { graphQlParams ->
    MainScope().promise {
        JSON.parse(httpClient.post(url) {
            headers { this["Authorization"] = "Bearer $token" }
            body = TextContent(JSON.stringify(graphQlParams), ContentType.Application.Json)
        })
    }
}
