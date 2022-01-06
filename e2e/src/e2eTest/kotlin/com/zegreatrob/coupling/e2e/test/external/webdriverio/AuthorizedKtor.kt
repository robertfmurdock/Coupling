package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.sdk.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

external val process: dynamic

private const val userEmail = "test@test.tes"

suspend fun authorizedSdk(username: String = userEmail) = AuthorizedSdk(
    authorizedKtorClient(username),
    username
)

private suspend fun authorizedKtorClient(username: String): HttpClient {
    val client = defaultClient().config {
        followRedirects = false
        val baseUrl = Url("${process.env.BASEURL}")

        defaultRequest {
            expectSuccess = false
            url {
                protocol = baseUrl.protocol
                host = baseUrl.host
                if(protocol != URLProtocol.HTTPS) {
                    port = baseUrl.port
                }
                encodedPath = "${baseUrl.encodedPath}$encodedPath"
            }
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    client.get<HttpResponse>("/test-login") {
        parameter("username", username)
        parameter("password", "pw")
    }

    console.log("ktor logged in for $username")
    return client
}

inline fun <T> withSdk(crossinline objectSetup: (AuthorizedSdk) -> T): suspend (Unit) -> T = {
    val sdk = authorizedSdk()
    objectSetup(sdk)
}


class AuthorizedSdk(val client: HttpClient, val userEmail: String) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {

        override val client get() = client

        override fun basename() = process.env.BASENAME.unsafeCast<String?>() ?: ""
    }) {
}