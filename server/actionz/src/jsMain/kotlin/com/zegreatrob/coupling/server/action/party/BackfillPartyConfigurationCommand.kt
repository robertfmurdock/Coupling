package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfill
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class BackfillPartyConfigurationCommand(val stage: String, val partyId: PartyId, val runId: String) {
    interface Dispatcher {
        val partyConfigurationBackfill: PartyConfigurationBackfill

        suspend fun perform(command: BackfillPartyConfigurationCommand) = with(command) {
            partyConfigurationBackfill.backfill(stage, partyId, runId)
        }
    }
}
