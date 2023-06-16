package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackRepository {
    suspend fun exchangeCodeForAccessToken(code: String): SlackTeamAccess
}
