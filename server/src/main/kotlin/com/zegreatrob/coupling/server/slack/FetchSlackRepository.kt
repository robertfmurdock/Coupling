package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.action.slack.SlackRepository.AccessTokenResult
import com.zegreatrob.coupling.server.express.Config
import kotlin.io.encoding.ExperimentalEncodingApi

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

    override suspend fun sendMessage(channel: String, token: String) {
        client.sendMessage("Spin!", channel, token)
    }
}
