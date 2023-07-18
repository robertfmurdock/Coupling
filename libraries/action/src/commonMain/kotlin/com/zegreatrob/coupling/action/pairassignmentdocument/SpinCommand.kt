package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class SpinCommand(val partyId: PartyId, val playerIds: NotEmptyList<String>, val pinIds: List<String>) {
    interface Dispatcher {
        suspend fun perform(command: SpinCommand): VoidResult
    }
}
