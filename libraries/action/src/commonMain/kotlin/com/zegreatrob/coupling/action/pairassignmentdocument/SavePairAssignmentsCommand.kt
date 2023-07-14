package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class SavePairAssignmentsCommand(
    val partyId: PartyId,
    val pairAssignments: PairAssignmentDocument,
) {
    interface Dispatcher {
        suspend fun perform(command: SavePairAssignmentsCommand): VoidResult
    }
}
