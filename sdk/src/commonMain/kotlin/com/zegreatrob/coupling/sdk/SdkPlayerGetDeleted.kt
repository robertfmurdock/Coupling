package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyGQLComponent.RetiredPlayerList

interface SdkPlayerGetDeleted : SdkApi, GqlQueryComponent {
    override suspend fun getDeleted(partyId: PartyId) = performQueryGetComponent(partyId, RetiredPlayerList)
        ?.partyData
        ?.retiredPlayers
        ?: emptyList()
}
