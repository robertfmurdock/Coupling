package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class CreateSecretCommand(val partyId: PartyId) :
    SimpleSuspendAction<CreateSecretCommand.Dispatcher, Pair<Secret, String>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: CreateSecretCommand): Pair<Secret, String>?
    }
}
