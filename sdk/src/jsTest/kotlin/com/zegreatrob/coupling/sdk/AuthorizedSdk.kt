package com.zegreatrob.coupling.sdk

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

const val primaryAuthorizedUsername = "couplingtestuser@gmail.com"
val primaryTestPassword = getEnv("COUPLING_PRIMARY_TEST_PASSWORD") ?: ""

expect fun getEnv(name: String): String?

val primaryAuthorizedSdkDeferred by lazy {
    MainScope().async {
        authorizedSdk(primaryAuthorizedUsername, primaryTestPassword)
            .apply { deleteAnyDisplayedTribes() }
    }
}

const val altAuthorizedUsername = "couplingtestuser.alt@gmail.com"
val altTestPassword = getEnv("COUPLING_ALT_TEST_PASSWORD") ?: ""

val altAuthorizedSdkDeferred by lazy {
    MainScope().async {
        authorizedSdk(altAuthorizedUsername, altTestPassword)
            .apply { deleteAnyDisplayedTribes() }
    }
}

private suspend fun Sdk.deleteAnyDisplayedTribes() = with(tribeRepository) {
    getTribes().forEach {
        delete(it.data.id)
    }
}

private suspend fun authorizedSdk(username: String, password: String) = generateAccessToken(username, password)
    .let { token -> SdkSingleton({ token }, buildClient()) }

suspend fun authorizedSdk() = primaryAuthorizedSdkDeferred.await()

val generalPurposeClient = HttpClient {
    install(ContentNegotiation) { json() }
    install(WebSockets)
    setupPlatformSpecificKtorSettings()
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

expect fun setupPlatformSpecificKtorSettings()

private val baseUrl = Url("https://localhost/local/")

private val ktorLogger = KotlinLogging.logger("ktor")

private fun buildClient(): HttpClient {
    setupPlatformSpecificKtorSettings()

    val client = defaultClient(null).config {
        followRedirects = false
        expectSuccess = false
        defaultRequest {
            url(baseUrl.toString())
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = ktorLogger.info { message }
            }
            level = LogLevel.INFO
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
            append("client_secret", getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("username", username)
            append("password", password)
            append("audience", "https://localhost/api")
            append("scope", "email")
        }
    ).body<JsonObject>()

    return result["access_token"]?.jsonPrimitive?.content ?: ""
}
