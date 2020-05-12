package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

class NewTribeCommand : SimpleSuspendAction<NewTribeCommandDispatcher, Tribe> {
    override val perform = link(NewTribeCommandDispatcher::perform)
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
