package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class NewPartyCommand : SimpleSuspendAction<NewPartyCommandDispatcher, Party?> {
    override val performFunc = link(NewPartyCommandDispatcher::perform)
}

interface NewPartyCommandDispatcher {
    suspend fun perform(command: NewPartyCommand) = newParty()

    private fun newParty() = Party(
        id = PartyId(""),
        name = "New Party",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

}
