package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument

interface SlackRepository {
    suspend fun exchangeCodeForAccessToken(code: String): AccessTokenResult
    suspend fun sendSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument)

    sealed interface AccessTokenResult {
        data class Success(val access: SlackTeamAccess) : AccessTokenResult
        data class RemoteError(val error: String?) : AccessTokenResult
        data class Unknown(val exception: Throwable) : AccessTokenResult
    }
}
