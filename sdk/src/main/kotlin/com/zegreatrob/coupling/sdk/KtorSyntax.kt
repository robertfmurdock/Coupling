package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import org.w3c.dom.Window
import org.w3c.dom.get

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

        js("global.window")
            .unsafeCast<Window?>()
            ?.let { window ->
                url {
                    protocol = URLProtocol.createOrDefault(window.location.protocol)
                    host = window.location.host
                    encodedPath = "${window["basename"]}/$encodedPath"
                }
            }
    }
}

private val defaultClient = defaultClient()

interface KtorSyntax {
    val client: HttpClient get() = defaultClient
}