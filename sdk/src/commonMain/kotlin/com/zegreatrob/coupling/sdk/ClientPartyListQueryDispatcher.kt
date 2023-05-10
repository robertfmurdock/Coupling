package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.PartyListQuery
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax

interface ClientPartyListQueryDispatcher : PartyRecordSyntax, PartyListQuery.Dispatcher {
    override suspend fun perform(query: PartyListQuery): List<Party> = getPartyRecords().map(Record<Party>::data)
}
