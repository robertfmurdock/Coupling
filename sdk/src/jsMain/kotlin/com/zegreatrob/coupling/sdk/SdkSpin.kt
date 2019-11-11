package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.js.Json

interface SdkSpin : AxiosSyntax {
    fun requestSpinAsync(tribeId: TribeId, players: List<Player>) = GlobalScope.async {
        axios.postAsync<Json>("/api/${tribeId.value}/spin", players.map { it.toJson() }.toTypedArray())
            .await()
            .toPairAssignmentDocument()
    }

}