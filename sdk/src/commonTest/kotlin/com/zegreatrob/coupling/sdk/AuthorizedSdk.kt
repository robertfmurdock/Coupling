@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

const val primaryAuthorizedUsername = "couplingtestuser@gmail.com"
val primaryTestPassword = getEnv("COUPLING_PRIMARY_TEST_PASSWORD") ?: ""

expect fun getEnv(name: String): String?

val primaryAuthorizedSdkDeferred by lazy {
    GlobalScope.async {
        sdk(primaryAuthorizedUsername, primaryTestPassword)
            .apply { deleteAnyDisplayedParties() }
    }
}

const val altAuthorizedUsername = "couplingtestuser.alt@gmail.com"
val altTestPassword = getEnv("COUPLING_ALT_TEST_PASSWORD") ?: ""

val altAuthorizedSdkDeferred by lazy {
    GlobalScope.async {
        sdk(altAuthorizedUsername, altTestPassword)
            .apply { deleteAnyDisplayedParties() }
    }
}

private suspend fun ActionCannon<CouplingSdkDispatcher>.deleteAnyDisplayedParties() = fire(graphQuery { partyList() })
    ?.partyList
    ?.forEach { fire(DeletePartyCommand(it.data.id)) }

private suspend fun sdk(username: String, password: String) = generateAccessToken(username, password)
    .let { token -> couplingSdk({ token }, buildClient()) }

suspend fun sdk(): ActionCannon<CouplingSdkDispatcher> = primaryAuthorizedSdkDeferred.await()

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
    defaultRequest { header("X-Request-Id", "${uuid4()}") }
}

expect fun setupPlatformSpecificKtorSettings()

private val baseUrl = Url("https://localhost/local/")

private val ktorLogger = KotlinLogging.logger("ktor")

fun buildClient(): HttpClient {
    setupPlatformSpecificKtorSettings()

    val client = defaultClient(null).config {
        followRedirects = false
        expectSuccess = false
        defaultRequest { url("$baseUrl") }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = ktorLogger.info { message }
            }
            level = LogLevel.ALL
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
        },
    ).body<JsonObject>()

    return result["access_token"]?.jsonPrimitive?.content ?: ""
}
