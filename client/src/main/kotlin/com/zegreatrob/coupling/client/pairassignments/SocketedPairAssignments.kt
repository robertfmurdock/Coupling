package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.CouplingWebsocket
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.disconnectedMessage
import com.zegreatrob.coupling.client.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.StateSetter
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useMemo
import react.useState

data class SocketedPairAssignments(
    val tribe: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val controls: Controls<PairAssignmentsCommandDispatcher>,
    val allowSave: Boolean
) : DataPropsBind<SocketedPairAssignments>(socketedPairAssignments)

val socketedPairAssignments = tmFC<SocketedPairAssignments> { (tribe, players, originalPairs, controls, allowSave) ->
    val (pairAssignments, setPairAssignments) = useState(originalPairs)

    val (message, setMessage) = useState(disconnectedMessage)
    val onMessageFunc: (Message) -> Unit = { handleMessage(it, setMessage, setPairAssignments) }

    val auth0Data = useAuth0Data()

    var token by useState("")

    useEffect {
        MainScope().launch { token = auth0Data.getAccessTokenSilently() }
    }

    if (token.isNotBlank()) {
        child(CouplingWebsocket(tribe.id, onMessage = onMessageFunc, token = token) {
            val updatePairAssignments = useMemo(controls.dispatchFunc) {
                updatePairAssignmentsFunc(setPairAssignments, controls.dispatchFunc, tribe.id)
            }
            child(PairAssignments(tribe, players, pairAssignments, updatePairAssignments, controls, message, allowSave))
        })
    } else {
        div()
    }
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
    tribeId: PartyId
) = { new: PairAssignmentDocument ->
    setPairAssignments(new)
    dispatchFunc({ SavePairAssignmentsCommand(tribeId, new) }, {}).invoke()
}
