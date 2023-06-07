package com.zegreatrob.coupling.action.secret

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DeleteSecretCommand(val partyId: PartyId, val secret: Secret) :
    SimpleSuspendAction<DeleteSecretCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeleteSecretCommand): VoidResult
    }
}
