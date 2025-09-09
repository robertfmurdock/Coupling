package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.ApolloClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders.UserAgent
import io.ktor.serialization.kotlinx.json.json
import kotlin.uuid.Uuid

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
        header("X-Request-ID", traceId ?: Uuid.random())
        url(hostUrl)
    }
}

interface KtorSyntax {
    val client: HttpClient
    val apolloClient: ApolloClient
    suspend fun getIdToken(): String
}
