package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackRepository {
    suspend fun exchangeCodeForAccessToken(code: String): SlackTeamAccess
    suspend fun sendMessage(channel: String, token: String)
}
