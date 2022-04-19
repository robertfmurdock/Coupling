package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

data class ProposeNewPairsCommand(val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendResultAction<ProposeNewPairsCommandDispatcher, PairAssignmentDocument> {
    override val performFunc = link(ProposeNewPairsCommandDispatcher::perform)
}

interface ProposeNewPairsCommandDispatcher {
    suspend fun perform(command: ProposeNewPairsCommand): Result<PairAssignmentDocument>
}
