package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.sdk.ServerRequestSpin
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>)

interface RequestSpinActionDispatcher : ServerRequestSpin {

    suspend fun RequestSpinAction.perform(): PairAssignmentDocument =
            requestSpinAsync(tribeId, players)
                    .await()

}
