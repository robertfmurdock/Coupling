package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackRepository {
    suspend fun exchangeCodeForAccessToken(code: String): AccessTokenResult
    suspend fun sendMessage(channel: String, token: String)

    sealed interface AccessTokenResult {
        data class Success(val access: SlackTeamAccess) : AccessTokenResult
        data class RemoteError(val error: String?) : AccessTokenResult
        data class Unknown(val exception: Throwable) : AccessTokenResult
    }
}
