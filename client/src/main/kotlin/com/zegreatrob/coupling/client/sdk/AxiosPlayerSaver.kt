package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.PlayerSaver
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface AxiosPlayerSaver : PlayerSaver {
    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        val (tribeId, player) = tribeIdPlayer
        axios.post("/api/${tribeId.value}/players/", player.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
    }
}