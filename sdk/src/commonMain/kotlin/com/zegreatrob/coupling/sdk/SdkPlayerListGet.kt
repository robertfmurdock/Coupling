package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PlayerList

interface SdkPlayerListGet : SdkApi, GqlQueryComponent {
    suspend fun getPlayers(partyId: PartyId) = performQueryGetComponent(partyId, PlayerList)
        ?.partyData
        ?.playerList
        ?: emptyList()
}
