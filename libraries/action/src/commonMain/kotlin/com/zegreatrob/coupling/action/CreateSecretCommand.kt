package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret

data class CreateSecretCommand(val partyId: PartyId) :
    SimpleSuspendResultAction<CreateSecretCommand.Dispatcher, Pair<Secret, String>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: CreateSecretCommand): Result<Pair<Secret, String>>
    }
}
