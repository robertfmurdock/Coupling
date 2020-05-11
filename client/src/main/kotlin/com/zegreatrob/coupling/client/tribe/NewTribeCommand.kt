package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

class NewTribeCommand : SuspendAction<NewTribeCommandDispatcher, Tribe> {
    override suspend fun execute(dispatcher: NewTribeCommandDispatcher) = dispatcher.perform()
}

interface NewTribeCommandDispatcher {
    fun perform() = newTribe().successResult()

    private fun newTribe() = Tribe(
        id = TribeId(""),
        name = "New Tribe",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

}
