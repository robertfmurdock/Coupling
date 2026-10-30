package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class BeginDiscordOAuthCommand(val partyId: PartyId, val challenge: String) {
    fun interface Dispatcher {
        suspend fun perform(command: BeginDiscordOAuthCommand): DiscordOAuthState?
    }
}

data class DiscordOAuthState(val state: String, val expiresAt: String)
