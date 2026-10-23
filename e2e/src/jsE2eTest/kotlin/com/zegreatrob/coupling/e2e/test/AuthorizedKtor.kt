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
        level = LogLevel.HEADERS
        logger = object : Logger {
            override fun log(message: String) = ktorLogger.info { message }
        }
    }
}

private val ktorLogger = KotlinLogging.logger("ktor")
private val auth0ClientSecret = Process.getEnv("AUTH0_CLIENT_SECRET") ?: ""

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
    ktorLogger.info {
        "Auth0 token request input: passwordPresent=${password.isNotEmpty()}, clientSecretPresent=${auth0ClientSecret.isNotEmpty()}"
    }
    val result = generalPurposeClient.submitForm(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", auth0ClientSecret)
            append("username", username)
            append("password", password)
            append("audience", "https://localhost/api")
            append("scope", "email")
        },
    ).body<JsonObject>()

    val token = result["access_token"]?.jsonPrimitive?.content
    if (token == null) {
        val error = result["error"]?.jsonPrimitive?.content
        val description = result["error_description"]?.jsonPrimitive?.content
        ktorLogger.warn { "Auth0 token request rejected: error=$error, description=$description" }
    }
    return token ?: ""
}

fun authorizedSdk(token: String) = couplingSdk({ token }, buildClientWithToken())
