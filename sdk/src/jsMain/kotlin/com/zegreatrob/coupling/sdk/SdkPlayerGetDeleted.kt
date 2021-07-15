package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import kotlinx.serialization.json.decodeFromDynamic

interface SdkPlayerGetDeleted : PlayerListGetDeleted, GqlQueryComponent {
    override suspend fun getDeleted(tribeId: TribeId) =
        performQueryGetComponent(tribeId, TribeGQLComponent.RetiredPlayerList) {
            couplingJsonFormat.decodeFromDynamic<List<JsonPlayerRecord>>(it)
        }?.map(JsonPlayerRecord::toModel) ?: emptyList()
}
