package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeDelete

interface SdkTribeDelete : TribeDelete, GqlSyntax, GraphQueries {
    override suspend fun delete(partyId: PartyId): Boolean = doQuery(
        mutations.deleteTribe,
        mapOf("tribeId" to partyId.value),
        "deleteTribe"
    ) { it: Boolean? -> it } ?: false
}
