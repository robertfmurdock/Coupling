package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkSpin : AxiosSyntax {
    fun requestSpinAsync(tribeId: TribeId, players: List<Player>) =
        axios.post("/api/${tribeId.value}/spin", players.map { it.toJson() }.toTypedArray())
            .then<PairAssignmentDocument> { it.data.unsafeCast<Json>().toPairAssignmentDocument() }
            .asDeferred()

}