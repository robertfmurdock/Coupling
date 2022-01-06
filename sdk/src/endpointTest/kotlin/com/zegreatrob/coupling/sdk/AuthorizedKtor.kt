package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.server.Process
import com.zegreatrob.coupling.stubmodel.uuidString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration

external val process: dynamic

private const val userEmail = "test@test.tes"

suspend fun authorizedKtorSdk(username: String = "${uuidString()}-$userEmail") = AuthorizedKtorSdk(
    authorizedKtorClient(username)
)

private val generalPurposeClient = HttpClient {
    install(JsonFeature)
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

val accessTokenDeferred by lazy { MainScope().async { generateAccessToken() } }

private suspend fun authorizedKtorClient(username: String): HttpClient {
    val accessToken = accessTokenDeferred.await()
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
            header("Authorization", "Bearer $accessToken")
        }
        install(Logging) {
            val ktorLogger = KotlinLogging.logger("ktor")
            logger = object : Logger {
                override fun log(message: String) {
                    ktorLogger.info { message }
                }
            }
            level = LogLevel.ALL
        }
    }

    console.log("ktor logged in for $username")
    return client
}

private suspend fun generateAccessToken(): String? {
    val result = generalPurposeClient.submitForm<JsonObject>(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", Process.getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("audience", "https://localhost/api")
        }
    )

    return result["access_token"]?.jsonPrimitive?.content
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