package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.couplingWebsocket
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.RSetState
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
        val updatePairAssignments = useUpdatePairAssignmentsMemo(setPairAssignments, sendMessage)

        pairAssignments(
            tribe,
            players,
            message.currentPairAssignments ?: pairAssignments,
            updatePairAssignments,
            controls,
            message,
            allowSave
        )
    }
}

private fun useUpdatePairAssignmentsMemo(
    setPairAssignments: RSetState<PairAssignmentDocument?>,
    sendMessage: ((Message) -> Unit)?
) = useMemo(
    { updatePairAssignmentsFunc(setPairAssignments, sendMessage) },
    arrayOf(sendMessage)
)

private fun updatePairAssignmentsFunc(
    setPairAssignments: RSetState<PairAssignmentDocument?>,
    sendMessage: ((Message) -> Unit)?
) = { new: PairAssignmentDocument ->
    setPairAssignments(new)
    if (sendMessage != null)
        sendMessage(PairAssignmentAdjustmentMessage(new))
}
