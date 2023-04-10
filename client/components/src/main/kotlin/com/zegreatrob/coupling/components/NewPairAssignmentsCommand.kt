package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class NewPairAssignmentsCommand(val partyId: PartyId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendAction<NewPairAssignmentsCommandDispatcher, Unit?> {
    override val performFunc = link(NewPairAssignmentsCommandDispatcher::perform)
}

interface NewPairAssignmentsCommandDispatcher {
    suspend fun perform(query: NewPairAssignmentsCommand): Unit?
}
