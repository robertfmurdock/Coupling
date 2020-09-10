package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.couplingWebsocket
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.RProps
import react.RSetState
import react.useMemo
import react.useState

data class SocketedPairAssignmentsProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val dispatchFunc: DispatchFunc<out SavePairAssignmentsCommandDispatcher>,
    val pathSetter: (String) -> Unit
) : RProps

val SocketedPairAssignments = reactFunction<SocketedPairAssignmentsProps> { props ->
    val (tribe, players, originalPairs, commandFunc, pathSetter) = props
    val (pairAssignments, setPairAssignments) = useState(originalPairs)

    couplingWebsocket(props.tribe.id, "https:" == window.location.protocol) { message, sendMessage ->
        val updatePairAssignments = useUpdatePairAssignmentsMemo(setPairAssignments, sendMessage)

        pairAssignments(
            tribe,
            players,
            message.currentPairAssignments ?: pairAssignments,
            updatePairAssignments,
            commandFunc,
            message,
            pathSetter
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
