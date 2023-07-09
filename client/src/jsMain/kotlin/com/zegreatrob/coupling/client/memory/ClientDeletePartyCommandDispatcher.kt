package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax

interface ClientDeletePartyCommandDispatcher : PartyIdDeleteSyntax, DeletePartyCommand.Dispatcher {
    override suspend fun perform(command: DeletePartyCommand) = command.partyId.deleteIt().voidResult()
}
