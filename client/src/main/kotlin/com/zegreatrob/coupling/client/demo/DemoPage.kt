package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.pairAssignments
import com.zegreatrob.coupling.client.pairassignments.spin.prepareSpin
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PinConfig
import com.zegreatrob.coupling.client.pin.PinConfigProps
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.tribe.TribeConfig
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.client.tribe.TribeConfigProps
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinx.html.DIV
import react.dom.RDOMBuilder
import react.dom.div
import react.key

interface NoOpDispatcher : TribeConfigDispatcher, PlayerConfigDispatcher, PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher, NewPairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C, response: (R) -> Unit
    ): () -> Unit = {}
}

val DemoPage = reactFunction<PageProps> {
    frameRunner(DemoAnimationState.generateSequence(), 1.0) { state: DemoAnimationState ->
        div {
            when (state) {
                Start -> +"Starting..."
                ShowIntro -> +"Alright, here's an example of how you might use the app."
                is MakeTribe -> tribeConfigFrame(state)
                is AddPlayer -> playerConfigFrame(state)
                is AddPin -> pinConfigFrame(state)
                is CurrentPairs -> pairAssignmentsFrame(state)
                is PrepareToSpin -> prepareSpinFrame(state)
            }
        }
    }
}

private fun RDOMBuilder<DIV>.prepareSpinFrame(state: PrepareToSpin) = prepareSpin(
    state.tribe,
    state.players,
    state.pins,
    state.pins.map { it.id },
    {},
    {},
    {}
)

private fun RDOMBuilder<DIV>.tribeConfigFrame(state: MakeTribe) = child(
    TribeConfig, TribeConfigProps(state.tribe, noOpDispatchFunc)
) { attrs.key = "$state" }

private fun RDOMBuilder<DIV>.playerConfigFrame(state: AddPlayer) = child(
    PlayerConfig,
    PlayerConfigProps(state.tribe, state.newPlayer, state.players, {}, noOpDispatchFunc)
) { attrs.key = "$state" }

private fun RDOMBuilder<DIV>.pinConfigFrame(state: AddPin) = child(
    PinConfig, PinConfigProps(state.tribe, state.newPin, state.pins, {}, noOpDispatchFunc)
) {
    attrs.key = "$state"
}

private fun RDOMBuilder<DIV>.pairAssignmentsFrame(state: CurrentPairs) = pairAssignments(
    state.tribe,
    state.players,
    state.pairAssignments,
    {},
    Controls(noOpDispatchFunc) {},
    CouplingSocketMessage("", emptySet()),
    state.allowSave
)
