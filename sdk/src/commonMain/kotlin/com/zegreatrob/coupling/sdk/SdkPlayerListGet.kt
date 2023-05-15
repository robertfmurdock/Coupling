package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PlayerList

interface SdkPlayerListGet : SdkApi, GqlQueryComponent {
    override suspend fun getPlayers(partyId: PartyId) =
        performQueryGetComponent(partyId, PlayerList) { it?.toDomain()?.partyData?.playerList }
            ?: emptyList()

    private fun toModel(it: List<JsonPlayerRecord>) = it.map(JsonPlayerRecord::toModel)
}
