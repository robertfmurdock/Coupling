package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.express.Config

class FetchSlackRepository : SlackRepository {

    private val client = FetchSlackClient(Config.slackClientId, Config.secretSigningSecret, slackRedirectUri())

    override suspend fun exchangeCodeForAccessToken(code: String) =
        toSlackTeamAccess(client.exchangeCodeForAccess(code))

    private fun toSlackTeamAccess(result: dynamic) = SlackTeamAccess(
        teamId = result.team.id as String,
        accessToken = result.access_token as String,
        appId = result.app_id as String,
        slackUserId = result.authed_user.id as String,
    )
}
