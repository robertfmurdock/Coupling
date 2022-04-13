package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PlayerList

interface SdkPlayerListGet : PlayerListGet, GqlQueryComponent {
    override suspend fun getPlayers(tribeId: PartyId) = performQueryGetComponent(tribeId, PlayerList, ::toModel)
        ?: emptyList()

    private fun toModel(it: List<JsonPlayerRecord>) = it.map(JsonPlayerRecord::toModel)
}
