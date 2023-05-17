package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PartyData

interface SdkPartyGet : SdkApi, GqlQueryComponent {
    suspend fun getPartyRecord(partyId: PartyId) = performQueryGetComponent(partyId, PartyData)
        ?.partyData
        ?.party
}
