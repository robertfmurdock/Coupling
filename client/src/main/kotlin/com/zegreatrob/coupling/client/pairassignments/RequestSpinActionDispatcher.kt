package com.zegreatrob.coupling.client.pairassignments


import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>)

interface RequestSpinActionDispatcher {

    suspend fun RequestSpinAction.perform(): PairAssignmentDocument =
            axios.post("/api/${tribeId.value}/spin", players.map { it.toJson() }.toTypedArray())
                    .then<PairAssignmentDocument> { it.data.unsafeCast<Json>().toPairAssignmentDocument() }
                    .asDeferred()
                    .await()

}
