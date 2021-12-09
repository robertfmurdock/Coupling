package com.zegreatrob.coupling.client.demo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

private val demoTribe = Tribe(id = TribeId("${uuid4()}"), name = "The Simpsons")

private val player1 = Player(name = "Homer")
private val player2 = Player(name = "Marge")
private val player3 = Player(name = "Bart")
private val player4 = Player(name = "Lisa")
private val player5 = Player(name = "Maggie")
private val player6 = Player(name = "Santa's Lil Helper")

private val players = listOf(
    player1,
    player2,
    player3,
    player4,
    player5,
    player6,
)

private val pins = listOf(
    Pin(name = "watchman", icon = "eye")
)

sealed class DemoAnimationState {

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> = listOfPairs()
            .runningFold(Frame<DemoAnimationState>(Start, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private fun listOfPairs() = listOf(Pair(ShowIntro, 3000)) +
                makeTribeSequence().map { it to 100 } +
                makePlayerSequence().map { it to 100 } +
                makePinSequence().map { it to 100 } +
                (CurrentPairs(demoTribe, players, pins) to 100)
    }
}

object Start : DemoAnimationState()

object ShowIntro : DemoAnimationState()

fun makeTribeSequence() = demoTribe.name.rangeOfStringLength().map { index ->
    MakeTribe(Tribe(id = demoTribe.id, name = demoTribe.name?.substring(0, index)))
}

fun makePlayerSequence() = players.flatMapIndexed { playerIndex, player ->
    player.name.rangeOfStringLength().map { index ->
        AddPlayer(demoTribe, player.copy(name = player.name.substring(0, index)), players.subList(0, playerIndex))
    }
} + AddPlayer(demoTribe, players.last(), players)


fun makePinSequence() = pins.flatMapIndexed { pinIndex, pin ->
    pin.name.rangeOfStringLength().map { index ->
        AddPin(demoTribe, pin.copy(name = pin.name.substring(0, index)), pins.subList(0, pinIndex))
    }
} + AddPin(demoTribe, pins.last(), pins)

private fun String?.rangeOfStringLength() = (0..(this ?: "").length)

data class MakeTribe(val tribe: Tribe) : DemoAnimationState()

data class AddPlayer(val tribe: Tribe, val newPlayer: Player, val players: List<Player>) : DemoAnimationState()

data class AddPin(val tribe: Tribe, val newPin: Pin, val pins: List<Pin>) : DemoAnimationState()

data class CurrentPairs(val tribe: Tribe, val players: List<Player>, val pins: List<Pin>) : DemoAnimationState()
