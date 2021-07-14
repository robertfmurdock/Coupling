package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayerRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import kotlin.js.Json

interface SdkPlayerGetDeleted : PlayerListGetDeleted, GqlQueryComponent {
    override suspend fun getDeleted(tribeId: TribeId) =
        performQueryGetComponent(tribeId, TribeGQLComponent.RetiredPlayerList) {
            it.unsafeCast<Array<Json>?>()?.map(Json::toPlayerRecord)
        } ?: emptyList()

}
