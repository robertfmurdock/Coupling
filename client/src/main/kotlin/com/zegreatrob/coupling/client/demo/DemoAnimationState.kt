package com.zegreatrob.coupling.client.demo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
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
    Pin(id = "", name = "watchman", icon = "eye")
)

private val pairAssignments = PairAssignmentDocument(
    PairAssignmentDocumentId(""),
    DateTime.now(),
    listOf(
        pairOf(player1, player4).withPins(emptyList()),
        pairOf(player2, player5).withPins(pins),
        pairOf(player6).withPins(emptyList()),
    )
)

sealed class DemoAnimationState {

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> = listOfPairs()
            .runningFold(Frame<DemoAnimationState>(Start, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private fun listOfPairs(): List<Pair<DemoAnimationState, Int>> = listOf(Pair(ShowIntro, 3000)) +
                makeTribeSequence().pairWithDurations(800, 100) +
                makePlayerSequence() +
                makePinSequence().pairWithDurations(800, 100) +
                (CurrentPairs(demoTribe, players, pins, null, false) to 100) +
                (PrepareToSpin(demoTribe, players.map { it to false }, pins) to 1500) +
                (PrepareToSpin(demoTribe, players.map { it to true }, pins) to 1500) +
                (PrepareToSpin(demoTribe, players.map { it to (it != player3) }, pins) to 1500) +
                (CurrentPairs(demoTribe, players, pins, pairAssignments, true) to 1500) +
                (CurrentPairs(demoTribe, players, pins, pairAssignments, false) to 10000)
    }

}

private fun <T> List<T>.pairWithDurations(firstDuration: Int, duration: Int) = mapIndexed { index, it ->
    it to if (index == 0) firstDuration else duration
}

object Start : DemoAnimationState()

object ShowIntro : DemoAnimationState()

fun makeTribeSequence() = demoTribe.name.rangeOfStringLength().map { index ->
    MakeTribe(Tribe(id = demoTribe.id, name = demoTribe.name?.substring(0, index)))
}

fun makePlayerSequence() = players.flatMapIndexed { playerIndex, player ->
    playerNameSequence(player, players.subList(0, playerIndex))
        .pairWithDurations(750, 100)
} + (AddPlayer(demoTribe, players.last(), players) to 100)

private fun playerNameSequence(
    player: Player,
    playersSoFar: List<Player>
) = player.name.rangeOfStringLength().map { index ->
    AddPlayer(demoTribe, player.copy(name = player.name.substring(0, index)), playersSoFar)
}

fun makePinSequence() = pins.flatMapIndexed { pinIndex, pin ->
    pin.name.rangeOfStringLength().map { index ->
        AddPin(demoTribe, pin.copy(name = pin.name.substring(0, index)), pins.subList(0, pinIndex))
    }
} + AddPin(demoTribe, pins.last(), pins)

private fun String?.rangeOfStringLength() = (0..(this ?: "").length)

data class MakeTribe(val tribe: Tribe) : DemoAnimationState()

data class AddPlayer(val tribe: Tribe, val newPlayer: Player, val players: List<Player>) : DemoAnimationState()

data class AddPin(val tribe: Tribe, val newPin: Pin, val pins: List<Pin>) : DemoAnimationState()

data class CurrentPairs(
    val tribe: Tribe,
    val players: List<Player>,
    val pins: List<Pin>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean
) : DemoAnimationState()

data class PrepareToSpin(val tribe: Tribe, val players: List<Pair<Player, Boolean>>, val pins: List<Pin>) :
    DemoAnimationState()
