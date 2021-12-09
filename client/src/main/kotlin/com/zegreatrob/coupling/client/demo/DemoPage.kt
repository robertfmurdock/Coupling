package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.pairAssignments
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
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.SuspendAction
import react.dom.div
import react.key

interface NoOpDispatcher : TribeConfigDispatcher, PlayerConfigDispatcher, PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C, response: (R) -> Unit
    ): () -> Unit = {}
}

val DemoPage = reactFunction<PageProps> {
    frameRunner(DemoAnimationState.generateSequence(), 1.0) { state: DemoAnimationState ->
        when (state) {
            Start -> div { +"Starting..." }
            ShowIntro -> div { +"Alright, here's an example of how you might use the app." }
            is MakeTribe -> div {
                child(TribeConfig, TribeConfigProps(state.tribe, noOpDispatchFunc)) { attrs.key = "$state" }
            }
            is AddPlayer -> div {
                child(
                    PlayerConfig,
                    PlayerConfigProps(state.tribe, state.newPlayer, state.players, {}, noOpDispatchFunc)
                ) {
                    attrs.key = "$state"
                }
            }
            is AddPin -> div {
                child(PinConfig, PinConfigProps(state.tribe, state.newPin, state.pins, {}, noOpDispatchFunc)) {
                    attrs.key = "$state"
                }
            }
            is CurrentPairs -> div {
                pairAssignments(
                    state.tribe,
                    state.players,
                    null,
                    {},
                    Controls(noOpDispatchFunc) {},
                    CouplingSocketMessage("", emptySet()),
                    false
                )
            }
        }
    }
}
