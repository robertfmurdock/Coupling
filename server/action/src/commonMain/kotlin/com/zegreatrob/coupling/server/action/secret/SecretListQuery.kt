package com.zegreatrob.coupling.server.action.secret

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.repository.secret.SecretListGet
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object SecretListQuery : SimpleSuspendAction<SecretListQuery.Dispatcher, List<PartyRecord<Secret>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : CurrentPartyIdSyntax {

        val secretRepository: SecretListGet

        suspend fun perform(query: SecretListQuery) = currentPartyId.let { secretRepository.getSecrets(it) }
    }
}
