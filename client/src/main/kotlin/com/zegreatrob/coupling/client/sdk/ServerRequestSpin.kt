package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface ServerRequestSpin {
    fun requestSpinAsync(tribeId: TribeId, players: List<Player>) =
        axios.post("/api/${tribeId.value}/spin", players.map { it.toJson() }.toTypedArray())
            .then<PairAssignmentDocument> { it.data.unsafeCast<Json>().toPairAssignmentDocument() }
            .asDeferred()

}