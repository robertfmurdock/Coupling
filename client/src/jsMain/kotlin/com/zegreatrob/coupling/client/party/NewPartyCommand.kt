package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class NewPartyCommand : SimpleSuspendAction<NewPartyCommandDispatcher, PartyDetails?> {
    override val performFunc = link(NewPartyCommandDispatcher::perform)
}

interface NewPartyCommandDispatcher {
    suspend fun perform(command: NewPartyCommand) = newParty()
}

fun newParty() = PartyDetails(
    id = PartyId(""),
    defaultBadgeName = "Default",
    alternateBadgeName = "Alternate",
    name = "New Party",
)
