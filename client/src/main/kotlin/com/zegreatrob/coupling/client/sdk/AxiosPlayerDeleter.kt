package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.model.player.PlayerDeleter
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface AxiosPlayerDeleter : PlayerDeleter {

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean {
        axios.delete("/api/${tribeId.value}/players/$playerId")
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
        return true
    }

}