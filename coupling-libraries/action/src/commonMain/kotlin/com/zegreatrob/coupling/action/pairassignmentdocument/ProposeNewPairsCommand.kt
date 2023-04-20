package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

data class ProposeNewPairsCommand(val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendResultAction<ProposeNewPairsCommand.Dispatcher, PairAssignmentDocument> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: ProposeNewPairsCommand): Result<PairAssignmentDocument>
    }
}
