package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.slack.external.webclient.ChatPostOptions
import com.zegreatrob.coupling.server.slack.external.webclient.WebClient
import js.core.jso

class FetchSlackRepository : SlackRepository {

    private val client = FetchSlackClient(Config.slackClientId, Config.slackClientSecret, slackRedirectUri())

    override suspend fun exchangeCodeForAccessToken(code: String) =
        toSlackTeamAccess(client.exchangeCodeForAccess(code))

    override fun sendMessage(channel: String, token: String) {
        WebClient(token).chat.postMessage(spinMessage(channel))
    }

    private fun spinMessage(channel: String): ChatPostOptions = jso {
        this.text = "Spin!"
        this.channel = channel
    }

    private fun toSlackTeamAccess(result: dynamic) = SlackTeamAccess(
        teamId = result.team.id as String,
        accessToken = result.access_token as String,
        appId = result.app_id as String,
        slackUserId = result.authed_user.id as String,
    )
}
