package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.party.PartyIntegrationSave

interface ServerSaveSlackIntegrationCommandDispatcher : SaveSlackIntegrationCommand.Dispatcher {

    val partyRepository: PartyIntegrationSave

    override suspend fun perform(command: SaveSlackIntegrationCommand) = with(command) {
        partyRepository.save(partyId.with(PartyIntegration(team, channel)))
        VoidResult.Accepted
    }
}
