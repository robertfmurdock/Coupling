package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.couplingWebsocket
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.RProps
import react.RSetState
import react.useMemo
import react.useState
import kotlin.js.json

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
        val updatePairAssignments = useMemo(
            { updatePairAssignmentsFunc(setPairAssignments, sendMessage) },
            arrayOf(sendMessage)
        )
        pairAssignments(tribe, players, pairAssignments, updatePairAssignments, commandFunc, message, pathSetter)
    }
}

private fun updatePairAssignmentsFunc(
    setPairAssignments: RSetState<PairAssignmentDocument?>,
    sendMessage: (Any) -> Unit
) = { new: PairAssignmentDocument ->
    setPairAssignments(new)
    sendMessage(JSON.stringify(json("updatedPairs" to new.toJson())))
}
