package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

fun defaultClient() = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(couplingJsonFormat)
    }
    install(UserAgent) {
        agent = "CouplingSdk"
    }
}

private val defaultClient = defaultClient()

interface KtorSyntax {
    val client: HttpClient get() = defaultClient
}