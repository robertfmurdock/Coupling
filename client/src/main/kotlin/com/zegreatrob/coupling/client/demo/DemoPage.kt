package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.frameRunner
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.tribe.TribeConfig
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.client.tribe.TribeConfigProps
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.SuspendAction
import react.dom.div
import react.key

interface NoOpDispatcher : TribeConfigDispatcher, PlayerConfigDispatcher

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C, response: (R) -> Unit
    ): () -> Unit = {}
}

val DemoPage = reactFunction<PageProps> {
    frameRunner(DemoAnimationState.generateSequence(), 1.0) { thing: DemoAnimationState ->
        when (thing) {
            Start -> div { +"Starting..." }
            ShowIntro -> div { +"Alright, here's an example of how you might use the app." }
            is MakeTribe -> div {
                child(TribeConfig, TribeConfigProps(thing.tribe, noOpDispatchFunc)) {
                    attrs.key = thing.tribe.toString()
                }
            }
            is AddPlayer1 -> div {
                child(
                    PlayerConfig,
                    PlayerConfigProps(thing.tribe, thing.player, emptyList(), {}, noOpDispatchFunc)
                ) {
                    attrs.key = thing.player.toString()
                }
            }
        }
    }
}
