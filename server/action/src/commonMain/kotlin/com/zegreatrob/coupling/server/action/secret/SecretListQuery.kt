package com.zegreatrob.coupling.server.action.secret

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.repository.secret.SecretListGet
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SecretListQuery(val partyId: PartyId) :
    SimpleSuspendAction<SecretListQuery.Dispatcher, List<PartyRecord<Secret>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {

        val secretRepository: SecretListGet

        suspend fun perform(query: SecretListQuery) = query.partyId.let { secretRepository.getSecrets(it) }
    }
}
