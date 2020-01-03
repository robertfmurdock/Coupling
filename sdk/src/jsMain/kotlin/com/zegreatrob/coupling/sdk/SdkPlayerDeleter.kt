package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkPlayerDeleter : PlayerDelete, AxiosSyntax {

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean {
        axios.delete("/api/tribes/${tribeId.value}/players/$playerId")
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
        return true
    }

}