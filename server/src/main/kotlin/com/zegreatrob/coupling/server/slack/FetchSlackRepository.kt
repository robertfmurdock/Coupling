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
                ),
            )
    }

    override suspend fun updateSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument) {
        val pairTimestamp = pairs.date.unixMillisDouble / 1_000
        val conversationHistory = client.getConversationHistory(
            channel = channel,
            accessToken = token,
            latest = pairTimestamp - 0.5,
            oldest = pairTimestamp + 0.5,
        )

        if (conversationHistory.ok == false) {
            throw Exception("Conversation History error: " + conversationHistory.error)
        }

        val messageToUpdate = conversationHistory.messages
            ?.minByOrNull { abs(pairTimestamp - it.ts.toDouble()) }
            ?.ts
        if (messageToUpdate != null) {
            val response = client.updateMessage(
                accessToken = token,
                channel = channel,
                ts = messageToUpdate,
                text = "Update!",
                blocks = pairs.toSlackBlocks(),
            )
            if (response.ok == false) {
                throw Exception("Update Message error: " + response.error)
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
        "accessory" to json(
            "type" to "image",
            "image_url" to "${Config.clientUrl}html/a9612ac3fc17807e372f.svg",
            "alt_text" to "coupling logo",
        ),
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
