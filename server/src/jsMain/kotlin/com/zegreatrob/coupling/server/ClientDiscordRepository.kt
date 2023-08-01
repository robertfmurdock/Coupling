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
import com.zegreatrob.coupling.server.discord.WebhookInformation
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

class ClientDiscordRepository(private val discordClient: DiscordClient, private val clientUrl: String) :
    DiscordRepository {

    override suspend fun exchangeForWebhook(code: String) = when (val result = discordClient.getAccessToken(code)) {
        is SuccessfulAccessResponse -> DiscordRepository.ExchangeResult.Success(
            DiscordTeamAccess(
                webhook = result.webhook.toDomain(),
                result.accessToken,
                result.refreshToken,
            ),
        )

        is ErrorAccessResponse -> DiscordRepository.ExchangeResult.Error(result.error, result.errorDescription)
    }

    private fun WebhookInformation.toDomain() = DiscordWebhook(
        id = id,
        token = token,
        channelId = channelId,
        guildId = guildId,
    )

    override suspend fun sendSpinMessage(
        webhook: DiscordWebhook,
        newPairs: PairAssignmentDocument,
    ): String? = when (val result = sendMessage(newPairs, webhook)) {
        is MessageResponseData -> result.id
        is ErrorAccessResponse -> null.also {
            theLogger.error {
                mapOf("error" to result.error, "description" to result.errorDescription)
            }
        }
    }

    override suspend fun deleteMessage(webhook: DiscordWebhook, deadPairs: PairAssignmentDocument) {
        deadPairs.discordMessageId?.let {
            discordClient.deleteWebhookMessage(
                messageId = it,
                webhookId = webhook.id,
                webhookToken = webhook.token,
            )
        }
    }

    private suspend fun sendMessage(
        pairs: PairAssignmentDocument,
        webhook: DiscordWebhook,
    ) = pairs.discordMessageId?.let {
        discordClient.updateWebhookMessage(
            messageId = it,
            message = "",
            webhookId = webhook.id,
            webhookToken = webhook.token,
            embeds = pairs.toDiscordEmbeds(),
        )
    } ?: discordClient.sendWebhookMessage(
        message = "",
        webhookId = webhook.id,
        webhookToken = webhook.token,
        embeds = pairs.toDiscordEmbeds(),
    )

    private fun PairAssignmentDocument.toDiscordEmbeds() = listOf(
        DiscordEmbed(
            title = "**Couples for ${dateText()}**",
            description = pairs.toList().joinToString("\n\n") { " - ${it.pairFieldText()}" },
            imageUrl = "$clientUrl/images/logo.png",
        ),
    )
}
