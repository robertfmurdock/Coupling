package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinContent
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PinConfigContent
import com.zegreatrob.coupling.client.player.PlayerConfigContent
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.tribe.TribeConfigContent
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.child
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinx.css.*
import kotlinx.css.properties.border
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div

interface NoOpDispatcher : TribeConfigDispatcher, PlayerConfigDispatcher, PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher, NewPairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C, response: (R) -> Unit
    ): () -> Unit = {}
}

val DemoPage = FC<PageProps> {
    child(FrameRunner(DemoAnimationState.generateSequence(), 1.0) { state: DemoAnimationState ->
        div {
            cssDiv(css = {
                display = Display.inlineBlock
                border(8.px, BorderStyle.solid, rgb(94, 84, 102), 50.px)
                backgroundColor = Color.floralWhite
                padding(left = 42.px, right = 42.px)
                width = 40.em
            }) {
                +"DEMO"
            }

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
    })
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (tribe, players, pins) = state
    child(PrepareSpinContent(tribe, players, pins, pins.map { it.id }, {}, {}, {}))
}

private fun ChildrenBuilder.tribeConfigFrame(state: MakeTribe) {
    child(TribeConfigContent(state.tribe, true, {}, {}, {}))
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) =
    child(PlayerConfigContent(state.tribe, state.newPlayer, state.players, {}, {}, {}))

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) =
    child(PinConfigContent(state.tribe, state.newPin, state.pins, {}, {}, {}))

private fun ChildrenBuilder.pairAssignmentsFrame(state: CurrentPairs) = child(
    PairAssignments(
        state.tribe,
        state.players,
        state.pairAssignments,
        { },
        Controls(noOpDispatchFunc) {},
        CouplingSocketMessage("", emptySet()),
        state.allowSave
    )
)
