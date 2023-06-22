package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.server.action.slack.SlackGrantAccess
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.express.Config
import korlibs.time.DateFormat
import korlibs.time.DateTimeTz
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json
import kotlin.math.abs

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
        } else
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

    override suspend fun updateSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument) {
        val messageTs = findExistingMessage(token, channel, pairs)
        if (messageTs != null) {
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
        }
    }

    private suspend fun findExistingMessage(
        token: String,
        channel: String,
        pairs: PairAssignmentDocument,
    ): String? {
        val pairTimestamp = pairs.date.unixMillisDouble
        val conversationHistory = client.getConversationHistory(
            channel = channel,
            accessToken = token,
            oldest = pairTimestamp - 1000,
            latest = pairTimestamp + 2000,
        )

        if (conversationHistory.ok == false) {
            throw Exception("Conversation History error: " + conversationHistory.error)
        }

        return conversationHistory.messages
            ?.minByOrNull { abs(pairTimestamp - it.ts.toDouble()) }
            ?.ts
    }

    override suspend fun deleteSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument) {
        findExistingMessage(token, channel, pairs)
            ?.let { messageTs ->
                val response = client.deleteMessage(accessToken = token, channel = channel, ts = messageTs)
                if (response.ok == false) {
                    throw Exception("Delete Message error: " + response.error)
                }
            }
    }

    override suspend fun sendSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument) {
        client.postMessage(
            blocks = pairs.toSlackBlocks(),
            text = "Spin!",
            channel = channel,
            accessToken = token,
        )
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
        "fields" to pairs.map {
            json(
                "type" to "mrkdwn",
                "text" to it.pairFieldText(),
            )
        }.toTypedArray(),
    ),
).let(JSON::stringify)

private fun PinnedCouplingPair.pairFieldText() = listOfNotNull(
    callSign()?.let { "*$it*" },
    players.joinToString(" & ") { player -> player.player.name },
    pins.joinToString(" / ") { it.name }
        .ifEmpty { null }
        ?.let { "📍 $it" },
).joinToString("\n")

fun PairAssignmentDocument.dateText() = date.local.dateText()

private fun DateTimeTz.dateText() = "${format(DateFormat("MM/dd/YYYY"))} - ${format(DateFormat("HH:mm:ss"))}"
