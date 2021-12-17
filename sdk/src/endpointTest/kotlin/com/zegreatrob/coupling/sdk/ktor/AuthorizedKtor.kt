package com.zegreatrob.coupling.sdk.ktor

import com.zegreatrob.coupling.sdk.*
import com.zegreatrob.coupling.stubmodel.uuidString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

external val process: dynamic

private const val userEmail = "test@test.tes"

suspend fun authorizedKtorSdk(username: String = "${uuidString()}-$userEmail") = AuthorizedKtorSdk(
    authorizedKtorClient(username)
)

private suspend fun authorizedKtorClient(username: String): HttpClient {
    val client = defaultClient().config {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        val baseUrl = Url("${process.env.BASEURL}")

        defaultRequest {
            expectSuccess = false
            headers {
                @Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
                append(HttpHeaders.Accept, "text/plain")
                @Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
                append(HttpHeaders.Accept, "*/*")
            }
            url {
                host = baseUrl.host
                port = baseUrl.port
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

inline fun <T> withKtorSdk(crossinline objectSetup: (AuthorizedKtorSdk) -> T): suspend (Unit) -> T = {
    val sdk = authorizedKtorSdk()
    objectSetup(sdk)
}


class AuthorizedKtorSdk(val client: HttpClient) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {

        override val client get() = client

        override fun basename() = process.env.BASENAME.unsafeCast<String?>() ?: ""
    }) {
}