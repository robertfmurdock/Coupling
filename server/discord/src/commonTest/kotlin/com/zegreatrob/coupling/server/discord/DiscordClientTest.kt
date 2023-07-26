package com.zegreatrob.coupling.server.discord

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DiscordClientTest {

    @Test
    fun canExchangeCodeForAccessToken() = asyncSetup(object : ScopeMint() {
        var lastRequestData: HttpRequestData? = null
        val handle: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { request ->
            lastRequestData = request
            respond(
                content = JSON.stringify(kotlinext.js.require("./expected-access-response.json")),
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        val httpClient = HttpClient(engine = MockEngine(handler = handle)) {
            install(Logging) {
                level = LogLevel.ALL
            }
        }
        val discordClientId = "1234567"
        val discordClientSecret = "7654321"
        val code = "INguEoKmDrGNcSLKOHQ0N1xFwgz9vt"
        val client = DiscordClient(
            httpClient = httpClient,
            clientId = discordClientId,
            clientSecret = discordClientSecret,
            host = "sandbox.coupling.zegreatrob.com",
        )
    }) exercise {
        client.getAccessToken(code)
    } verify { result ->
        lastRequestData?.headers?.getAll("Authorization")
            .assertIsEqualTo(listOf("Basic MTIzNDU2Nzo3NjU0MzIx"))
        result.assertIsEqualTo(
            SuccessfulAccessResponse(
                accessToken = "ATATATATATATATATATTATAAT",
                tokenType = "Bearer",
                expiresIn = 604800,
                refreshToken = "RTRTRTRTRTRTRTTRTTRRRTTTRRTRTRTR",
                scope = "",
                webhook = WebhookInformation(
                    token = "asdfasdfasdfasdfasdfadfasdfasdfasdfasdfasdfsadfasdf",
                    id = "1133818487601627189",
                    applicationId = "1133538666661281862",
                    name = "coupling-sandbox",
                    url = "https://discord.com/api/webhooks/1133818487601627189/asdfasdfasdfasdfasdfadfasdfasdfasdfasdfasdfsadfasdf",
                    channelId = "1133765536904392776",
                    type = 1,
                    avatar = null,
                    guildId = "692733747052937289",
                ),
            ),
        )
    }

    @Test
    fun canHandleFailureToExchange() = asyncSetup(object : ScopeMint() {
        val expectedResponse = ErrorAccessResponse(
            error = "invalid_grant",
            errorDescription = "Invalid \"code\" in request.",
        )
        var lastRequestData: HttpRequestData? = null
        val handle: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { request ->
            lastRequestData = request
            respond(
                status = HttpStatusCode.BadRequest,
                content = Json.encodeToString(expectedResponse),
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        val httpClient = HttpClient(engine = MockEngine(handler = handle)) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
        val discordClientId = "1234567"
        val discordClientSecret = "7654321"
        val code = "8675309"
        val client = DiscordClient(
            httpClient = httpClient,
            clientId = discordClientId,
            clientSecret = discordClientSecret,
            host = "sandbox.coupling.zegreatrob.com",
        )
    }) exercise {
        client.getAccessToken(code)
    } verify { result ->
        result.assertIsEqualTo(expectedResponse)
        lastRequestData?.headers?.getAll("Authorization")
            .assertIsEqualTo(listOf("Basic MTIzNDU2Nzo3NjU0MzIx"))
    }
}
