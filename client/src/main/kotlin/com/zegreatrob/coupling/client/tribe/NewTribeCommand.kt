package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class NewTribeCommand : SimpleSuspendAction<NewTribeCommandDispatcher, Tribe?> {
    override val performFunc = link(NewTribeCommandDispatcher::perform)
}

interface NewTribeCommandDispatcher {
    suspend fun perform(command: NewTribeCommand) = newTribe()

    private fun newTribe() = Tribe(
        id = TribeId(""),
        name = "New Tribe",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

}
