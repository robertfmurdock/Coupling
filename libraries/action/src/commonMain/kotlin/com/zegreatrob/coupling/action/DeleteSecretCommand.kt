package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret

data class DeleteSecretCommand(val partyId: PartyId, val secret: Secret) :
    SimpleSuspendResultAction<DeleteSecretCommand.Dispatcher, Boolean> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeleteSecretCommand): Result<Boolean>
    }
}
