package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.ApolloClient
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.uuid.Uuid

fun defaultClient(hostUrl: String, traceId: Uuid? = null, engine: HttpClientEngine? = null): HttpClient = buildClient(engine) {
    this.expectSuccess = false
    this.defaultRequest {
        this.header(HttpHeaders.UserAgent, "CouplingSdk")
        header("X-Request-ID", traceId ?: Uuid.random())
        this.url(hostUrl)
    }
}

private fun buildClient(
    engine: HttpClientEngine? = null,
    block: HttpClientConfig<*>.() -> Unit,
): HttpClient = engine?.let { HttpClient(engine, block) } ?: HttpClient(block)

interface KtorSyntax {
    val client: HttpClient
    val apolloClient: ApolloClient
    suspend fun getIdToken(): String
}
