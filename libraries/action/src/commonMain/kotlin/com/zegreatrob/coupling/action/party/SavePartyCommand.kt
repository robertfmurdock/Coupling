package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SavePartyCommand(
    val partyId: PartyId,
    val party: PartyDetails? = null,
    val players: List<Player> = emptyList(),
    val pins: List<Pin> = emptyList(),
) {
    constructor(party: PartyDetails) : this(party.id, party)

    fun interface Dispatcher {
        suspend fun perform(command: SavePartyCommand): VoidResult
    }
}
