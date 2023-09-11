package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class GrantDiscordAccessCommand(val code: String, val guildId: String, val partyId: PartyId) {
    fun interface Dispatcher {
        suspend fun perform(command: GrantDiscordAccessCommand): VoidResult
    }
}
