package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkPlayerGetDeleted : PlayerListGetDeleted, AxiosSyntax {
    override suspend fun getDeleted(tribeId: TribeId): List<Player> =
        axios.getList("/api/tribes/${tribeId.value}/players/retired")
            .then { it.map(Json::toPlayer) }
            .await()
}