package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.stubmodel.uuidString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration

external val process: dynamic

private const val userEmail = "test@test.tes"

suspend fun authorizedKtorSdk(username: String = "${uuidString()}-$userEmail") = AuthorizedKtorSdk(
    authorizedKtorClient(username)
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
                if (protocol != URLProtocol.HTTPS) {
                    port = baseUrl.port
                }
                encodedPath = "${baseUrl.encodedPath}$encodedPath"
            }
        }
        install(Logging) {
            KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
            val ktorLogger = KotlinLogging.logger("ktor")
            logger = object : Logger {
                override fun log(message: String) {
                    ktorLogger.info { message }
                }
            }
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