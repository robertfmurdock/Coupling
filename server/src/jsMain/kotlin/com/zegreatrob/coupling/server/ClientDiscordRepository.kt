package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.discord.DiscordRepository
import com.zegreatrob.coupling.server.discord.DiscordClient
import com.zegreatrob.coupling.server.discord.DiscordEmbed
import com.zegreatrob.coupling.server.discord.ErrorAccessResponse
import com.zegreatrob.coupling.server.discord.MessageResponseData
import com.zegreatrob.coupling.server.discord.SuccessfulAccessResponse
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.slack.dateText
import com.zegreatrob.coupling.server.slack.pairFieldText
import io.github.oshai.kotlinlogging.KotlinLogging

fun clientDiscordRepository() = ClientDiscordRepository(
    DiscordClient(
        clientId = Config.discordClientId,
        clientSecret = Config.discordClientSecret,
        host = "${Config.publicUrl}${Config.clientBasename}",
    ),
    Config.clientUrl,
)

private val theLogger by lazy { KotlinLogging.logger("DiscordLogger") }

class ClientDiscordRepository(private val discordClient: DiscordClient, private val clientUrl: String) : DiscordRepository {

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
        when (
            val result = discordClient.sendWebhookMessage(
                message = "",
                webhookId = webhook.id,
                webhookToken = webhook.token,
                embeds = listOf(
                    DiscordEmbed(
                        title = "**Couples for ${newPairs.dateText()}**",
                        description = newPairs.pairs.toList().joinToString("\n\n") { " - " + it.pairFieldText() },
                        imageUrl = "$clientUrl/images/logo.png",
                    ),
                ),
            )
        ) {
            is MessageResponseData -> Unit
            is ErrorAccessResponse -> theLogger.error {
                mapOf("error" to result.error, "description" to result.errorDescription)
            }
        }
    }
}
