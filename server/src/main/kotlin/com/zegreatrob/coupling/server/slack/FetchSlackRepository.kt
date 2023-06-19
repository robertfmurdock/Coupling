package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.action.slack.SlackRepository.AccessTokenResult
import com.zegreatrob.coupling.server.express.Config
import korlibs.time.DateFormat
import korlibs.time.DateTimeTz
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json

@OptIn(ExperimentalEncodingApi::class)
class FetchSlackRepository : SlackRepository {

    private val client = FetchSlackClient(Config.slackClientId, Config.slackClientSecret, slackRedirectUri())

    override suspend fun exchangeCodeForAccessToken(code: String) = runCatching { client.exchangeCodeForAccess(code) }
        .toResult()

    private fun Result<AccessResponse>.toResult() = map { it.toResult() }
        .getOrElse { AccessTokenResult.Unknown(it) }

    private fun AccessResponse.toResult(): AccessTokenResult {
        return if (ok == false) {
            AccessTokenResult.RemoteError(error)
        } else
            AccessTokenResult.Success(
                SlackTeamAccess(
                    teamId = team?.id ?: return AccessTokenResult.Unknown(Exception("Missing team id")),
                    accessToken = accessToken ?: return AccessTokenResult.Unknown(Exception("Missing access token")),
                    appId = appId ?: "",
                    slackUserId = authedUser?.id ?: "",
                ),
            )
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
