package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.action.discord.DiscordRepository
import com.zegreatrob.coupling.server.action.discord.DiscordWebhook
import com.zegreatrob.coupling.server.discord.DiscordClient
import com.zegreatrob.coupling.server.discord.ErrorAccessResponse
import com.zegreatrob.coupling.server.discord.SuccessfulAccessResponse
import com.zegreatrob.coupling.server.express.Config

fun discordRepositoryImpl() = DiscordRepositoryImpl(
    DiscordClient(
        clientId = Config.discordClientId,
        clientSecret = Config.discordClientSecret,
        host = "${Config.publicUrl}${Config.clientBasename}",
    ),
)

class DiscordRepositoryImpl(private val discordClient: DiscordClient) : DiscordRepository {

    override suspend fun exchangeForWebhook(code: String) = when (val result = discordClient.getAccessToken(code)) {
        is SuccessfulAccessResponse -> DiscordRepository.ExchangeResult.Success(
            result.webhook.let { DiscordWebhook(it.id, it.token) },
        )

        is ErrorAccessResponse -> DiscordRepository.ExchangeResult.Error(result.error, result.errorDescription)
    }
}
