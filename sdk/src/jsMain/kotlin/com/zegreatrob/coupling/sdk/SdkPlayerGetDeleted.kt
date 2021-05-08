package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.recordFor
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import kotlin.js.Json

interface SdkPlayerGetDeleted : PlayerListGetDeleted, GqlQueryComponent {
    override suspend fun getDeleted(tribeId: TribeId) =
        performQueryGetComponent(tribeId, TribeGQLComponent.RetiredPlayerList) {
            it.unsafeCast<Array<Json>?>()?.map { json ->
                val player = json.toPlayer()
                json.recordFor(tribeId.with(player)).copy(isDeleted = true)
            }
        } ?: emptyList()

}