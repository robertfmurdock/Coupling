package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class NewPairAssignmentsCommand(val partyId: PartyId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendAction<NewPairAssignmentsCommand.Dispatcher, Unit?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: NewPairAssignmentsCommand): Unit?
    }
}
