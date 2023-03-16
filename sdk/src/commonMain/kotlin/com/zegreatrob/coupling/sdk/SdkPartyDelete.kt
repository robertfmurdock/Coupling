package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyDelete

interface SdkPartyDelete : PartyDelete, GqlSyntax, GraphQueries {
    override suspend fun deleteIt(partyId: PartyId): Boolean = doQuery(
        mutations.deleteParty,
        mapOf("partyId" to partyId.value),
        "deleteParty",
    ) { it: Boolean? -> it } ?: false
}
