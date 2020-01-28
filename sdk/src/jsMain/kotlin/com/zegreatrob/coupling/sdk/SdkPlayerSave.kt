package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.repository.player.PlayerSave

interface SdkPlayerSave : PlayerSave, AxiosSyntax {
    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        val (tribeId, player) = tribeIdPlayer
        axios.postAsync<Unit>("/api/tribes/${tribeId.value}/players/", player.toJson()).await()
    }
}