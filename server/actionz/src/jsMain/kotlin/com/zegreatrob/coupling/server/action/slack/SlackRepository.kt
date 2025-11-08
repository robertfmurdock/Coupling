package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyId

interface SlackRepository :
    SlackGrantAccess,
    SlackSendSpin,
    SlackDeleteSpin

fun interface SlackDeleteSpin {
    suspend fun deleteSpinMessage(channel: String, token: String, pairs: PairingSet)
}

fun interface SlackSendSpin {
    suspend fun sendSpinMessage(channel: String, token: String, pairs: PairingSet, partyId: PartyId): String?
}

fun interface SlackGrantAccess {

    suspend fun exchangeCodeForAccessToken(code: String): Result

    sealed interface Result {
        data class Success(val access: SlackTeamAccess) : Result
        data class RemoteError(val error: String?) : Result
        data class Unknown(val exception: Throwable) : Result
    }
}
