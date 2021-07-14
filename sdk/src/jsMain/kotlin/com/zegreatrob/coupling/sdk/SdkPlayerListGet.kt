package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayerRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PlayerList
import kotlin.js.Json

interface SdkPlayerListGet : PlayerListGet, GqlQueryComponent {
    override suspend fun getPlayers(tribeId: TribeId) = performQueryGetComponent(tribeId, PlayerList) {
        it.unsafeCast<Array<Json>?>()
            ?.map(Json::toPlayerRecord)
    } ?: emptyList()
}
