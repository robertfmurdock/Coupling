package com.zegreatrob.coupling.repository.discord

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId

interface DiscordOAuthRepository {
    suspend fun savePending(stateHash: String, userId: UserId, partyId: PartyId, challenge: String, expiresAt: Long)
    suspend fun consume(stateHash: String, userId: UserId, partyId: PartyId, challenge: String): Boolean
}
