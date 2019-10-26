package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface PlayerSaveSyntax {

    suspend fun saveAsync(tribeId: TribeId, player: Player) =
        axios.post("/api/${tribeId.value}/players/", player.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()

}
