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
                    protocol = if(window.location.protocol == "http:") URLProtocol.HTTP else URLProtocol.HTTPS
                    host = window.location.hostname
                    window.location.port.toIntOrNull()?.let {
                        port = it
                    }
                    encodedPath = "${window["basename"]}/$encodedPath"
                }
            }
    }
}

private val defaultClient = defaultClient()

interface KtorSyntax {
    val client: HttpClient get() = defaultClient
}