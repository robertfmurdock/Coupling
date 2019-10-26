package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.sdk.ServerRequestSpin
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>)

interface RequestSpinActionDispatcher : ServerRequestSpin {

    suspend fun RequestSpinAction.perform(): PairAssignmentDocument =
        requestSpinAsync(tribeId, players)
            .await()

}
