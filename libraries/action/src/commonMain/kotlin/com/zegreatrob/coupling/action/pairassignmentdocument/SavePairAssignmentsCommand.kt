package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePairAssignmentsCommand(
    val partyId: PartyId,
    val pairAssignments: PairAssignmentDocument,
) : SimpleSuspendAction<SavePairAssignmentsCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePairAssignmentsCommand): VoidResult
    }
}
