package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PinList

interface SdkPinGet : SdkApi, GqlQueryComponent {
    override suspend fun getPins(partyId: PartyId) = performQueryGetComponent(partyId, PinList)
        ?.partyData
        ?.pinList
        ?: emptyList()
}
