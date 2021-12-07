package com.zegreatrob.coupling.client.demo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

val demoTribe = Tribe(id = TribeId("${uuid4()}"), name = "Your team name here")

private val player1 = Player(name = "Homer")

sealed class DemoAnimationState {

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> = listOfPairs()
            .runningFold(Frame<DemoAnimationState>(Start, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private fun listOfPairs() = listOf(Pair(ShowIntro, 3000)) +
                makeTribeSequence().map { it to 100 } +
                makePlayerSequence().map { it to 100 }
    }
}

object Start : DemoAnimationState()

object ShowIntro : DemoAnimationState()

fun makeTribeSequence() = demoTribe.name.rangeOfStringLength().map { index ->
    MakeTribe(Tribe(id = demoTribe.id, name = demoTribe.name?.substring(0, index)))
}

fun makePlayerSequence() = player1.name.rangeOfStringLength().map { index ->
    AddPlayer1(demoTribe, player1.copy(name = player1.name.substring(0, index)))
}

private fun String?.rangeOfStringLength() = (0..(this ?: "").length)

data class MakeTribe(val tribe: Tribe) : DemoAnimationState()

data class AddPlayer1(val tribe: Tribe, val player: Player) : DemoAnimationState()