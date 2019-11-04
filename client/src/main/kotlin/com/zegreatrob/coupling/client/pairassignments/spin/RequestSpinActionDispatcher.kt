package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.sdk.SdkSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>)

interface RequestSpinActionDispatcher : SdkSyntax {

    suspend fun RequestSpinAction.perform(): PairAssignmentDocument = with(sdk) {
        requestSpinAsync(tribeId, players)
            .await()
    }
}
