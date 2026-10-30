package com.zegreatrob.coupling.server.discord

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SimulatedDiscordTest {
    @Test
    fun exchangesSyntheticCodeForDeterministicDestination() = asyncSetup(object : ScopeMint() {
        val client = simulatedClient()
    }) exercise {
        client.getAccessToken("simulated:my-channel")
    } verify { result ->
        result.assertIsEqualTo(
            SuccessfulAccessResponse(
                accessToken = "simulated-access-my-channel",
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshToken = "simulated-refresh-my-channel",
                scope = "webhook.incoming",
                webhook = WebhookInformation(
                    token = "simulated-token-my-channel",
                    id = "simulated-webhook-my-channel",
                    applicationId = "simulated-app",
                    name = "Simulated Discord",
                    url = "https://discord.invalid/api/webhooks/simulated-webhook-my-channel/simulated-token-my-channel",
                    channelId = "my-channel",
                    type = 1,
                    avatar = null,
                    guildId = "simulated-guild-my-channel",
                ),
            ),
        )
        client.getAccessToken("simulated:my-channel").assertIsEqualTo(result)
    }

    @Test
    fun rejectsMalformedCodes() = asyncSetup(object : ScopeMint() {
        val client = simulatedClient()
    }) exercise {
        listOf("", "real-code", "simulated:", "simulated:bad/destination")
            .map { client.getAccessToken(it) }
    } verify { results ->
        results.assertIsEqualTo(
            List(4) { ErrorAccessResponse(error = "invalid_grant", errorDescription = "Expected simulated:<destination>.") },
        )
    }

    @Test
    fun sendsAndUpdatesMessagesWithStableIdsAndDeletesWithoutNetwork() = asyncSetup(object : ScopeMint() {
        val client = simulatedClient()
    }) exercise {
        client.sendWebhookMessage("hello", "simulated-webhook-channel", "simulated-token-channel", emptyList())
    } verify { result ->
        val sent = result as MessageResponseData
        sent.content.assertIsEqualTo("hello")
        sent.webhookId.assertIsEqualTo("simulated-webhook-channel")
        client.sendWebhookMessage("hello", sent.webhookId, "simulated-token-channel", emptyList())
            .assertIsEqualTo(sent)
        val differentMessage = client.sendWebhookMessage("different", sent.webhookId, "simulated-token-channel", emptyList()) as MessageResponseData
        differentMessage.id.assertIsNotEqualTo(sent.id)
        val updated = client.updateWebhookMessage(sent.id, "updated", sent.webhookId, "simulated-token-channel", emptyList()) as MessageResponseData
        updated.id.assertIsEqualTo(sent.id)
        updated.content.assertIsEqualTo("updated")
        client.deleteWebhookMessage(sent.id, sent.webhookId, "simulated-token-channel").assertIsEqualTo(true)
    }
}

private fun simulatedClient() = DiscordClient(
    clientId = "fake",
    clientSecret = "fake",
    host = "https://localhost",
    httpClient = DiscordMode.SIMULATED.httpClient(),
)
