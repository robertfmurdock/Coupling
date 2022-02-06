package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*

fun defaultClient(locationAndBasename: Pair<String, String>?) = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(couplingJsonFormat)
    }
    install(UserAgent) {
        agent = "CouplingSdk"
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    defaultRequest {
        expectSuccess = false

        locationAndBasename
            ?.let { (location, basename) ->
                url {
                    protocol = if (location.startsWith("http:")) URLProtocol.HTTP else URLProtocol.HTTPS
                    val locationUrl = Url(location)
                    host = locationUrl.host
                    port = locationUrl.port
                    encodedPath = "${basename}/$encodedPath"
                }
            }
    }
}

interface KtorSyntax {
    val client: HttpClient
    suspend fun getIdToken(): String
}
