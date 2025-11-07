@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.PartyIdListQuery
import com.zegreatrob.testmints.action.ActionCannon
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.Uuid

const val PRIMARY_AUTHORIZED_USER_EMAIL = "couplingtestuser@gmail.com"
val primaryTestPassword = getEnv("COUPLING_PRIMARY_TEST_PASSWORD") ?: ""

expect fun getEnv(name: String): String?

val primaryAuthorizedSdkDeferred by lazy {
    GlobalScope.async {
        sdk(PRIMARY_AUTHORIZED_USER_EMAIL, primaryTestPassword)
            .apply { deleteAnyDisplayedParties() }
    }
}

const val ALT_AUTHORIZED_USER_EMAIL = "couplingtestuser.alt@gmail.com"
val altTestPassword = getEnv("COUPLING_ALT_TEST_PASSWORD") ?: ""

val altAuthorizedSdkDeferred by lazy {
    GlobalScope.async {
        sdk(ALT_AUTHORIZED_USER_EMAIL, altTestPassword)
            .apply { deleteAnyDisplayedParties() }
    }
}

private suspend fun ActionCannon<CouplingSdkDispatcher>.deleteAnyDisplayedParties() = coroutineScope {
    fire(GqlQuery(PartyIdListQuery()))
        ?.partyList
        ?.map { it.id }
        ?.forEach { launch { fire(DeletePartyCommand(it)) } }
}

suspend fun sdk(username: String, password: String, engine: HttpClientEngine? = null, traceId: Uuid = Uuid.random()) = generateAccessToken(username, password)
    .let { token ->
        couplingSdk(
            { token },
            buildClient(engine, traceId),
            LoggingActionPipe(traceId),
        )
    }

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
    defaultRequest { header("X-Request-Id", "${Uuid.random()}") }
}

expect fun setupPlatformSpecificKtorSettings()

val baseUrl = Url("https://localhost")

private val ktorLogger = KotlinLogging.logger("ktor")

fun buildClient(engine: HttpClientEngine? = null, traceId: Uuid): HttpClient {
    setupPlatformSpecificKtorSettings()

    return defaultClient("$baseUrl", traceId, engine).config {
        this.followRedirects = false
        this.expectSuccess = false
        this.install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = ktorLogger.info { message }
            }
            level = LogLevel.BODY
        }
    }
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
