package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkPlayerGetter : PlayerGetter, AxiosSyntax {
    override suspend fun getPlayers(tribeId: TribeId): List<Player> =
        axios.getList("/api/${tribeId.value}/players")
            .then { it.map(Json::toPlayer) }
            .await()
}