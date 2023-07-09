package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingWebsocket
import com.zegreatrob.coupling.client.components.disconnectedMessage
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.components.pairassignments.create
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.Props
import react.StateSetter
import react.dom.html.ReactHTML.div
import react.useCallback
import react.useEffect
import react.useState

external interface SocketedPairAssignmentsProps<D> : Props
    where D : SavePairAssignmentsCommand.Dispatcher, D : DeletePairAssignmentsCommand.Dispatcher {
    var party: PartyDetails
    var players: List<Player>
    var pairAssignments: PairAssignmentDocument?
    var controls: Controls<D>
    var allowSave: Boolean
}

@ReactFunc
val SocketedPairAssignments by nfc<SocketedPairAssignmentsProps<*>> { (party, players, originalPairs, controls, allowSave) ->
    val (pairAssignments, setPairAssignments) = useState(originalPairs)
    val (message, setMessage) = useState(disconnectedMessage)
    val onMessageFunc: (Message) -> Unit = useCallback { handleMessage(it, setMessage, setPairAssignments) }
    val updatePairAssignments = useCallback(party.id, controls.dispatchFunc) { new: PairAssignmentDocument ->
        setPairAssignments(new)
        controls.dispatchFunc({ SavePairAssignmentsCommand(party.id, new) }, {}).invoke()
    }
    val auth0Data = useAuth0Data()
    var token by useState("")
    useEffect {
        MainScope().launch { token = auth0Data.getAccessTokenSilently() }
    }

    if (token.isNotBlank()) {
        CouplingWebsocket(
            partyId = party.id,
            onMessage = onMessageFunc,
            buildChild = {
                PairAssignments.create(
                    party = party,
                    players = players,
                    pairs = pairAssignments,
                    setPairs = updatePairAssignments,
                    controls = controls,
                    message = message,
                    allowSave = allowSave,
                )
            },
            token = token,
        )
    } else {
        div()
    }
}

private fun handleMessage(
    newMessage: Message,
    setMessage: StateSetter<CouplingSocketMessage>,
    setPairAssignments: StateSetter<PairAssignmentDocument?>,
) = when (newMessage) {
    is CouplingSocketMessage -> {
        setMessage(newMessage)
        newMessage.currentPairAssignments?.let { setPairAssignments(it) }
    }

    is PairAssignmentAdjustmentMessage -> setPairAssignments(newMessage.currentPairAssignments)
}
