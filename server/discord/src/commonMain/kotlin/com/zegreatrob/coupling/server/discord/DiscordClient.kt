package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.Parameters

class DiscordClient(httpClient: HttpClient, token: String) {
    private val httpClient = httpClient.config {
        defaultRequest {
            url("https://discord.com/api/")
            header("Authorization", "Bot $token")
        }
    }

    suspend fun sendMessage(channelId: String) {
        httpClient.submitForm(
            "channels/$channelId/messages\n",
            Parameters.build {
                append("content", "Hello nerds, I am a robot.")
            },
        )
    }
}
