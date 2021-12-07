package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

val demoTribe = Tribe(id = TribeId(""), name = "Your team name here")

sealed class DemoAnimationState {

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> = listOfPairs()
            .runningFold(Frame<DemoAnimationState>(Start, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private fun listOfPairs() = listOf(Pair(ShowIntro, 3000)) +
                makeTribeSequence().map { it to 2000 } +
                Pair(AddPlayer1(demoTribe, Player()), 4000)
    }
}

object Start : DemoAnimationState()

object ShowIntro : DemoAnimationState()

fun makeTribeSequence() = listOf(
    MakeTribe(Tribe(id = demoTribe.id)),
    MakeTribe(Tribe(id = demoTribe.id, name = demoTribe.name)),
)

data class MakeTribe(val tribe: Tribe) : DemoAnimationState()

data class AddPlayer1(val tribe: Tribe, val player: Player) : DemoAnimationState()