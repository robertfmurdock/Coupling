package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyGet
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PartyData

interface SdkPartyGet : PartyGet, GqlQueryComponent {
    override suspend fun getPartyRecord(partyId: PartyId) = performQueryGetComponent(
        partyId,
        PartyData,
        JsonPartyRecord::toModelRecord,
    )
}
