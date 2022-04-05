package com.zegreatrob.coupling.sdk

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.UserAgent
import io.ktor.serialization.kotlinx.json.*

fun defaultClient(locationAndBasename: Pair<String, String>?) = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    defaultRequest {
        expectSuccess = false
        header(UserAgent, "CouplingSdk")
        locationAndBasename
            ?.let { (location, basename) ->
                url {
                    protocol = if (location.startsWith("http:")) URLProtocol.HTTP else URLProtocol.HTTPS
                    val locationUrl = Url(location)
                    host = locationUrl.host
                    port = locationUrl.port
                    encodedPath = "${basename}$encodedPath"
                }
            }
    }
}

interface KtorSyntax {
    val client: HttpClient
    suspend fun getIdToken(): String
}
