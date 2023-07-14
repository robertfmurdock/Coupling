package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class SpinCommand(val partyId: PartyId, val playerIds: List<String>, val pinIds: List<String>) {
    interface Dispatcher {
        suspend fun perform(command: SpinCommand): VoidResult
    }
}
