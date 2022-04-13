package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class NewTribeCommand : SimpleSuspendAction<NewTribeCommandDispatcher, Party?> {
    override val performFunc = link(NewTribeCommandDispatcher::perform)
}

interface NewTribeCommandDispatcher {
    suspend fun perform(command: NewTribeCommand) = newParty()

    private fun newParty() = Party(
        id = PartyId(""),
        name = "New Party",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

}
