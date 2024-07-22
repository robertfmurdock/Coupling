package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SaveContributionCommand(
    val partyId: PartyId,
    val contributionList: List<ContributionInput>,
) {

    fun interface Dispatcher {
        suspend fun perform(command: SaveContributionCommand): VoidResult
    }
}
