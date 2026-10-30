package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.discord.DiscordOAuthRepository
import kotlin.time.Clock

class MemoryDiscordOAuthRepository(private val clock: Clock) : DiscordOAuthRepository {
    private data class Pending(val userId: UserId, val partyId: PartyId, val challenge: String, val expiresAt: Long)
    private val pending = mutableMapOf<String, Pending>()

    override suspend fun savePending(stateHash: String, userId: UserId, partyId: PartyId, challenge: String, expiresAt: Long) {
        check(stateHash !in pending)
        pending[stateHash] = Pending(userId, partyId, challenge, expiresAt)
    }

    override suspend fun consume(stateHash: String, userId: UserId, partyId: PartyId, challenge: String): Boolean {
        val record = pending[stateHash] ?: return false
        if (record.expiresAt <= clock.now().toEpochMilliseconds()) {
            pending.remove(stateHash)
            return false
        }
        if (record.userId != userId || record.partyId != partyId || record.challenge != challenge) return false
        pending.remove(stateHash)
        return true
    }
}
