package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.recordFor
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await

interface SdkPlayerGetDeleted : PlayerListGetDeleted, AxiosSyntax {
    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> =
        axios.getList("/api/tribes/${tribeId.value}/players/retired")
            .then { it.map { json -> json.recordFor(TribeIdPlayer(tribeId, json.toPlayer())).copy(isDeleted = true) } }
            .await()
}