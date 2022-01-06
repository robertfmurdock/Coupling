package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.CouplingWebsocket
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.disconnectedMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.StateSetter
import react.useMemo
import react.useState

data class SocketedPairAssignments(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val controls: Controls<PairAssignmentsCommandDispatcher>,
    val allowSave: Boolean
) : DataProps<SocketedPairAssignments> {
    override val component: TMFC<SocketedPairAssignments> get() = socketedPairAssignments
}

val socketedPairAssignments = tmFC<SocketedPairAssignments> { (tribe, players, originalPairs, controls, allowSave) ->
    val (pairAssignments, setPairAssignments) = useState(originalPairs)

    val (message, setMessage) = useState(disconnectedMessage)
    val onMessageFunc: (Message) -> Unit = { handleMessage(it, setMessage, setPairAssignments) }

    child(CouplingWebsocket(tribe.id, onMessage = onMessageFunc) {
        val updatePairAssignments = useMemo(controls.dispatchFunc) {
            updatePairAssignmentsFunc(setPairAssignments, controls.dispatchFunc, tribe.id)
        }
        child(PairAssignments(tribe, players, pairAssignments, updatePairAssignments, controls, message, allowSave))
    })
}

private fun handleMessage(
    newMessage: Message,
    setMessage: StateSetter<CouplingSocketMessage>,
    setPairAssignments: StateSetter<PairAssignmentDocument?>
) = when (newMessage) {
    is CouplingSocketMessage -> {
        setMessage(newMessage)
        newMessage.currentPairAssignments?.let { setPairAssignments(it) }
    }
    is PairAssignmentAdjustmentMessage -> setPairAssignments(newMessage.currentPairAssignments)
}

private fun updatePairAssignmentsFunc(
    setPairAssignments: StateSetter<PairAssignmentDocument?>,
    dispatchFunc: DispatchFunc<out PairAssignmentsCommandDispatcher>,
    tribeId: TribeId
) = { new: PairAssignmentDocument ->
    setPairAssignments(new)
    dispatchFunc({ SavePairAssignmentsCommand(tribeId, new) }, {}).invoke()
}
