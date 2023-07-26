package com.zegreatrob.coupling.server.discord

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlin.test.Test

class DiscordClientTest {

    @Test
    fun canTalkToDiscord() = asyncSetup(object : ScopeMint() {
        val httpClient = HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
        val token = ""
        val channelId = ""
        val client = DiscordClient(httpClient, token)
    }) exercise {
        client.sendMessage(channelId)
    } verify { result ->
        result.assertIsEqualTo(Unit)
    }
}
