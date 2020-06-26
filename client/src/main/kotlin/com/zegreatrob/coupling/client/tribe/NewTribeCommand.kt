package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

class NewTribeCommand : SimpleSuspendResultAction<NewTribeCommandDispatcher, Tribe> {
    override val performFunc = link(NewTribeCommandDispatcher::perform)
}

interface NewTribeCommandDispatcher {
    suspend fun perform(command: NewTribeCommand) = newTribe().successResult()

    private fun newTribe() = Tribe(
        id = TribeId(""),
        name = "New Tribe",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

}
