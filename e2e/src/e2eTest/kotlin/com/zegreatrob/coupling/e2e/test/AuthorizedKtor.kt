package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.coupling.server.Process
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

external val process: dynamic

const val primaryAuthorizedUsername = "couplingtestuser.e2e@gmail.com"
val primaryTestPassword = Process.getEnv("COUPLING_E2E_TEST_PASSWORD") ?: ""

val primaryAuthorizedSdkDeferred: Deferred<Sdk> by lazyDeferred {
    authorizedKtorSdk(primaryAuthorizedUsername, primaryTestPassword)
}

private suspend fun authorizedKtorSdk(username: String, password: String) =
    AuthorizedSdk(
        generateAccessToken(username, password)
    )

suspend fun authorizedKtorSdk() = primaryAuthorizedSdkDeferred.await()

private val generalPurposeClient = HttpClient {
    install(JsonFeature)
    install(Logging) {
        level = LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) = ktorLogger.info { message }
        }
    }
}

private val ktorLogger = KotlinLogging.logger("ktor")

private fun buildClientWithToken(): HttpClient {
    val client = defaultClient(null).config {
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
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) = ktorLogger.info { message }
            }
        }
    }
    return client
}

private suspend fun generateAccessToken(username: String, password: String): String {
    val result = generalPurposeClient.submitForm<JsonObject>(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", Process.getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("username", username)
            append("password", password)
            append("audience", "https://localhost/api")
            append("scope", "email")
        }
    )

    return result["access_token"]?.jsonPrimitive?.content ?: ""
}

fun AuthorizedSdk(token: String) = SdkSingleton({ token }, buildClientWithToken())
