package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.slack.SlackGrantAccess
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.express.Config
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json

@OptIn(ExperimentalEncodingApi::class)
class FetchSlackRepository : SlackRepository {

    private val client = FetchSlackClient(Config.slackClientId, Config.slackClientSecret, slackRedirectUri())

    override suspend fun exchangeCodeForAccessToken(code: String) = runCatching { client.exchangeCodeForAccess(code) }
        .toResult()

    private fun Result<AccessResponse>.toResult() = map { it.toResult() }
        .getOrElse { SlackGrantAccess.Result.Unknown(it) }

    private fun AccessResponse.toResult(): SlackGrantAccess.Result {
        return if (ok == false) {
            SlackGrantAccess.Result.RemoteError(error)
        } else {
            SlackGrantAccess.Result.Success(
                SlackTeamAccess(
                    teamId = team?.id ?: return SlackGrantAccess.Result.Unknown(Exception("Missing team id")),
                    accessToken = accessToken
                        ?: return SlackGrantAccess.Result.Unknown(Exception("Missing access token")),
                    appId = appId ?: "",
                    slackUserId = authedUser?.id ?: "",
                    slackBotUserId = botUserId
                        ?: return SlackGrantAccess.Result.Unknown(Exception("Missing bot user")),
                ),
            )
        }
    }

    private suspend fun updateMessage(
        token: String,
        channel: String,
        messageTs: String?,
        pairs: PairAssignmentDocument,
    ): MessageResponse {
        val response = client.updateMessage(
            accessToken = token,
            channel = channel,
            ts = messageTs,
            text = "Update!",
            blocks = pairs.toSlackBlocks(),
        )
        if (response.ok == false) {
            throw Exception("Update Message error: " + response.error)
        }
        return response
    }

    override suspend fun deleteSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument) {
        pairs.slackMessageId
            ?.let { messageTs ->
                val response = client.deleteMessage(accessToken = token, channel = channel, ts = messageTs)
                if (response.ok == false) {
                    throw Exception("Delete Message error: " + response.error)
                }
            }
    }

    override suspend fun sendSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument): String? {
        return (
            pairs.slackMessageId
                ?.let { slackMessageId -> updateMessage(token, channel, slackMessageId, pairs) }
                ?: client.postMessage(
                    blocks = pairs.toSlackBlocks(),
                    text = "Spin!",
                    channel = channel,
                    accessToken = token,
                )
            ).ts
    }
}

private fun PairAssignmentDocument.toSlackBlocks() = arrayOf(
    json(
        "type" to "header",
        "text" to json(
            "type" to "plain_text",
            "text" to "Couples for ${dateText()}",
            "emoji" to true,
        ),
    ),
    json(
        "type" to "section",
        "fields" to pairs.toList().map {
            json(
                "type" to "mrkdwn",
                "text" to it.pairFieldText(),
            )
        }.toTypedArray(),
    ),
).let(JSON::stringify)

fun PinnedCouplingPair.pairFieldText() = listOfNotNull(
    callSign().let { "*$it*" },
    players.toList().joinToString(" & ", transform = Player::name),
    pins.joinToString(" / ", transform = Pin::name)
        .ifEmpty { null }
        ?.let { "📍 $it" },
).joinToString("\n")

fun PairAssignmentDocument.dateText() = date.toLocalDateTime(TimeZone.currentSystemDefault()).dateText()

private fun LocalDateTime.dateText() = "$date - $time"
