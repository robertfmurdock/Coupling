package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.sdk.TribeGQLComponent.RetiredPlayerList

interface SdkPlayerGetDeleted : PlayerListGetDeleted, GqlQueryComponent {
    override suspend fun getDeleted(tribeId: TribeId) = performQueryGetComponent(tribeId, RetiredPlayerList, ::toModel)
        ?: emptyList()

    private fun toModel(it: List<JsonPlayerRecord>) = it.map(JsonPlayerRecord::toModel)
}
