package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json

interface SdkSpin : AxiosSyntax {
    suspend fun requestSpin(tribeId: TribeId, players: List<Player>) =
        axios.postAsync<Json>("/api/${tribeId.value}/spin", players.map { it.toJson() }.toTypedArray())
            .await()
            .toPairAssignmentDocument()
}