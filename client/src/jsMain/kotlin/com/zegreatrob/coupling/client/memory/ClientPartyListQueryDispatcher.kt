package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.client.PartyListQuery
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax

interface ClientPartyListQueryDispatcher :
    PartyRecordSyntax,
    PartyListQuery.Dispatcher {
    override suspend fun perform(query: PartyListQuery): List<PartyDetails> = getPartyRecords().map(Record<PartyDetails>::data)
}
