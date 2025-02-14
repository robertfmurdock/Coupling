package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

external val process: dynamic

const val PRIMARY_AUTHORIZED_USERNAME = "couplingtestuser.e2e@gmail.com"
val primaryTestPassword = Process.getEnv("COUPLING_E2E_TEST_PASSWORD") ?: ""

val primaryAuthorizedSdkDeferred by lazyDeferred {
    authorizedKtorCouplingSdk(PRIMARY_AUTHORIZED_USERNAME, primaryTestPassword)
}

private suspend fun authorizedKtorCouplingSdk(username: String, password: String) = authorizedSdk(
    generateAccessToken(username, password),
)

suspend fun authorizedKtorCouplingSdk() = primaryAuthorizedSdkDeferred.await()

private val generalPurposeClient = HttpClient {
    install(ContentNegotiation) { json() }
    install(Logging) {
        level = LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) = ktorLogger.info { message }
        }
    }
}

private val ktorLogger = KotlinLogging.logger("ktor")

private fun buildClientWithToken(): HttpClient {
    val client = defaultClient("${process.env.BASEURL}").config {
        followRedirects = false
        expectSuccess = false
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
    val result = generalPurposeClient.submitForm(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", Process.getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("username", username)
            append("password", password)
            append("audience", "https://localhost/api")
            append("scope", "email")
        },
    ).body<JsonObject>()

    return result["access_token"]?.jsonPrimitive?.content ?: ""
}

fun authorizedSdk(token: String) = couplingSdk({ token }, buildClientWithToken())
