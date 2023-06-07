package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax

interface ClientPartyListQueryDispatcher : PartyRecordSyntax, PartyListQuery.Dispatcher {
    override suspend fun perform(query: PartyListQuery): List<Party> = getPartyRecords().map(Record<Party>::data)
}
