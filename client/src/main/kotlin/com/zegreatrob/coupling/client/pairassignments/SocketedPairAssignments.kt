package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.couplingWebsocket
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.StateSetter
import react.useMemo
import react.useState

data class SocketedPairAssignmentsProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val controls: Controls<PairAssignmentsCommandDispatcher>,
    val allowSave: Boolean
) : RProps

val SocketedPairAssignments = reactFunction<SocketedPairAssignmentsProps> { props ->
    val (tribe, players, originalPairs, controls, allowSave) = props
    val (pairAssignments, setPairAssignments) = useState(originalPairs)

    couplingWebsocket(props.tribe.id) { message, sendMessage ->
        val updatePairAssignments = useUpdatePairAssignmentsMemo(
            setPairAssignments,
            sendMessage,
            controls.dispatchFunc,
            tribe.id
        )
        message.currentPairAssignments?.let { setPairAssignments(it) }
        pairAssignments(
            tribe,
            players,
            pairAssignments,
            updatePairAssignments,
            controls,
            message,
            allowSave
        )
    }
}

private fun useUpdatePairAssignmentsMemo(
    setPairAssignments: StateSetter<PairAssignmentDocument?>,
    sendMessage: ((Message) -> Unit)?,
    dispatchFunc: DispatchFunc<out PairAssignmentsCommandDispatcher>,
    tribeId: TribeId
) = useMemo(sendMessage, dispatchFunc) {
    updatePairAssignmentsFunc(setPairAssignments, sendMessage, dispatchFunc, tribeId)
}

private fun updatePairAssignmentsFunc(
    setPairAssignments: StateSetter<PairAssignmentDocument?>,
    sendMessage: ((Message) -> Unit)?,
    dispatchFunc: DispatchFunc<out PairAssignmentsCommandDispatcher>,
    tribeId: TribeId
) = { new: PairAssignmentDocument ->
    setPairAssignments(new)
    dispatchFunc(
        commandFunc = { SavePairAssignmentsCommand(tribeId, new) },
        response = {}
    ).invoke()
    if (sendMessage != null)
        sendMessage(PairAssignmentAdjustmentMessage(new))
}
