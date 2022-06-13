package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyDelete

interface SdkPartyDelete : PartyDelete, GqlSyntax, GraphQueries {
    override suspend fun deleteIt(partyId: PartyId): Boolean = doQuery(
        mutations.deleteTribe,
        mapOf("tribeId" to partyId.value),
        "deleteTribe"
    ) { it: Boolean? -> it } ?: false
}
