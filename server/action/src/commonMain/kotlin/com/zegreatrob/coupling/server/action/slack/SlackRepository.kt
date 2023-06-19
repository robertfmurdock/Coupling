package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument

interface SlackRepository : SlackGrantAccess, SlackSendSpin, SlackUpdateSpin

fun interface SlackUpdateSpin {
    suspend fun updateSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument)
}

fun interface SlackSendSpin {
    suspend fun sendSpinMessage(channel: String, token: String, pairs: PairAssignmentDocument)
}

fun interface SlackGrantAccess {

    suspend fun exchangeCodeForAccessToken(code: String): Result

    sealed interface Result {
        data class Success(val access: SlackTeamAccess) : Result
        data class RemoteError(val error: String?) : Result
        data class Unknown(val exception: Throwable) : Result
    }
}
