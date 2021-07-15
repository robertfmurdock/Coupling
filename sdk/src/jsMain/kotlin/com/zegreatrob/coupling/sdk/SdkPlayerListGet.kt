package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PlayerList
import kotlinx.serialization.json.decodeFromDynamic

interface SdkPlayerListGet : PlayerListGet, GqlQueryComponent {
    override suspend fun getPlayers(tribeId: TribeId) = performQueryGetComponent(tribeId, PlayerList) {
        if (it == null)
            emptyList()
        else
            couplingJsonFormat.decodeFromDynamic<List<JsonPlayerRecord>>(it)
    }?.map(JsonPlayerRecord::toModel) ?: emptyList()
}
