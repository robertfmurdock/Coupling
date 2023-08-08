package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders.UserAgent
import io.ktor.serialization.kotlinx.json.json

fun defaultClient(hostUrl: String, traceId: Uuid? = null) = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    expectSuccess = false
    defaultRequest {
        header(UserAgent, "CouplingSdk")
        header("X-Request-ID", traceId ?: uuid4())
        url(hostUrl)
    }
}

interface KtorSyntax {
    val client: HttpClient
    suspend fun getIdToken(): String
}
