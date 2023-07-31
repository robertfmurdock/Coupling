package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.discord.DiscordRepository
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
            DiscordTeamAccess(
                webhook = result.webhook.let { DiscordWebhook(it.id, it.token) },
                result.accessToken,
                result.refreshToken,
            ),
        )

        is ErrorAccessResponse -> DiscordRepository.ExchangeResult.Error(result.error, result.errorDescription)
    }

    override suspend fun sendSpinMessage(webhook: DiscordWebhook, newPairs: PairAssignmentDocument) {
        discordClient.sendWebhookMessage(
            message = "New Pairs! ${newPairs.date}",
            webhookId = webhook.id,
            webhookToken = webhook.token,
        )
    }
}
