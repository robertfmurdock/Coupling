package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*

fun defaultClient() = HttpClient {
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

        getLocationAndBasename()
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

expect fun getLocationAndBasename(): Pair<String, String>?

private val defaultClient = defaultClient()

interface KtorSyntax {
    val client: HttpClient get() = defaultClient
    suspend fun getIdToken(): String
}
