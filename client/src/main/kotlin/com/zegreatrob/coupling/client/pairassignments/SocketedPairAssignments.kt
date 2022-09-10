package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.components.CouplingWebsocket
import com.zegreatrob.coupling.components.disconnectedMessage
import com.zegreatrob.coupling.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.StateSetter
import react.dom.html.ReactHTML.div
import react.useCallback
import react.useEffect
import react.useState

data class SocketedPairAssignments<D>(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val controls: Controls<D>,
    val allowSave: Boolean
) : DataPropsBind<SocketedPairAssignments<D>>(socketedPairAssignments<D>())
    where D : SavePairAssignmentsCommandDispatcher, D : DeletePairAssignmentsCommandDispatcher

private fun <D> socketedPairAssignments()
    where D : SavePairAssignmentsCommandDispatcher, D : DeletePairAssignmentsCommandDispatcher =
    tmFC<SocketedPairAssignments<D>> { (party, players, originalPairs, controls, allowSave) ->
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
            add(
                CouplingWebsocket(
                    party.id,
                    onMessage = onMessageFunc,
                    buildChild = {
                        PairAssignments(
                            party,
                            players,
                            pairAssignments,
                            updatePairAssignments,
                            controls,
                            message,
                            allowSave
                        )
                            .create()
                    },
                    token = token
                )
            )
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
