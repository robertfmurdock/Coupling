package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.user.CouplingSocketMessage
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import react.RClass
import kotlin.test.Test

class PairAssignmentsTest {

    val tribe = Tribe(TribeId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs(): Unit = setup(object {
        val fellow = Player(id = "3", name = "fellow")
        val guy = Player(id = "2", name = "Guy")

        val rigby = Player(id = "1", name = "rigby")
        val nerd = Player(id = "4", name = "nerd")
        val pantsmaster = Player(id = "5", name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        var pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pairOf(Player(id = "0", name = "Tom"), Player(id = "z", name = "Jerry")),
                pairOf(fellow, guy)
            ).withPins()
        )
    }) exercise {
        shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                players,
                pairAssignments,
                { pairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    } verify { wrapper ->
        wrapper.find(PlayerRoster)
            .props()
            .players
            .assertIsEqualTo(
                listOf(rigby, nerd, pantsmaster)
            )
    }

    @Test
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = setup(object {
        val players = listOf(
            Player(id = "1", name = "rigby"),
            Player(id = "2", name = "Guy"),
            Player(id = "3", name = "fellow"),
            Player(id = "4", name = "nerd"),
            Player(id = "5", name = "pantsmaster")
        )
    }) exercise {
        shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                players,
                null,
                {},
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    } verify { wrapper ->
        wrapper.find(PlayerRoster)
            .props()
            .players
            .assertIsEqualTo(players)
    }

    @Test
    fun onClickSaveWillUseCouplingToSaveAndRedirectToCurrentPairAssignmentsPage() = setup(object {
        val pathSetterSpy = SpyData<String, Unit>()
        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = emptyList()
        )
        val dispatchFunc = StubDispatchFunc<SavePairAssignmentsCommandDispatcher>()
        val wrapper = shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                emptyList(),
                pairAssignments,
                {},
                dispatchFunc,
                CouplingSocketMessage("", emptyList()),
                pathSetter = pathSetterSpy::spyFunction
            )
        )
    }) exercise {
        wrapper.find(CurrentPairAssignmentsPanel).props()
            .onSave()
        dispatchFunc.simulateSuccess<SavePairAssignmentsCommand>()
    } verify {
        dispatchFunc.commandsDispatched<SavePairAssignmentsCommand>().size
            .assertIsEqualTo(1)

        pathSetterSpy.spyReceivedValues
            .assertContains("/${tribe.id.value}/pairAssignments/current/")
    }

    @Test
    fun onPlayerDropWillTakeTwoPlayersAndSwapTheirPlaces() = setup(object {
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2),
                pairOf(player3, player4)
            ).withPins()
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                emptyList(),
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull {
            it.pairs[0].toPair().asArray().toList()
                .assertIsEqualTo(listOf(player1, player3))
            it.pairs[1].toPair().asArray().toList()
                .assertIsEqualTo(listOf(player2, player4))
        }
    }

    @Test
    fun onPinDropWillTakeMovePinFromOnePairToAnother() = setup(object {
        val pin1 = stubPin()
        val pin2 = stubPin()
        val pair1 = pairOf(Player("1", name = "1"), Player("2", name = "2")).withPins(listOf(pin1))
        val pair2 = pairOf(Player("3", name = "3"), Player("4", name = "4")).withPins(listOf(pin2))
        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(pair1, pair2)
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                emptyList(),
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    }) exercise {
        pin1.dragTo(pair2, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull { pairs ->
            pairs.pairs[0]
                .assertIsEqualTo(pair1.copy(pins = emptyList()))
            pairs.pairs[1]
                .assertIsEqualTo(pair2.copy(pins = listOf(pin2, pin1)))
        }
    }

    @Test
    fun onPlayerDropTheSwapWillNotLosePinAssignments() = setup(object {
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pin1 = stubPin()
        val pin2 = stubPin()

        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2).withPins(listOf(pin1)),
                pairOf(player3, player4).withPins(listOf(pin2))
            )
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                emptyList(),
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull {
            it.pairs[0].pins
                .assertIsEqualTo(listOf(pin1))
            it.pairs[1].pins
                .assertIsEqualTo(listOf(pin2))
        }
    }

    @Test
    fun onPlayerDropWillNotSwapPlayersThatAreAlreadyPaired() = setup(object {
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2),
                pairOf(player3, player4)
            ).withPins()
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                emptyList(),
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    }) exercise {
        player4.dragTo(player3, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull {
            it.pairs[0].toPair().assertIsEqualTo(pairOf(player1, player2))
            it.pairs[1].toPair().assertIsEqualTo(pairOf(player3, player4))
        }
    }

    private fun Player.dragTo(target: Player, wrapper: ShallowWrapper<RClass<PairAssignmentsProps>>) {
        val targetProps = wrapper.find(CurrentPairAssignmentsPanel).props()

        targetProps.run {
            val targetPair = pairAssignments?.pairs?.first { pair -> pair.players.map { it.player }.contains(target) }!!

            onPlayerSwap(id!!, targetPair.players.first { it.player == target }, targetPair)
        }
    }

    private fun Pin.dragTo(targetPair: PinnedCouplingPair, wrapper: ShallowWrapper<RClass<PairAssignmentsProps>>) {
        val targetProps = wrapper.find(CurrentPairAssignmentsPanel).props()
        targetProps.onPinDrop(this.id!!, targetPair)
    }

    @Test
    fun passesDownTribeIdToServerMessage() = setup(object {
    }) exercise {
        shallow(
            PairAssignments,
            PairAssignmentsProps(
                tribe,
                listOf(),
                null,
                {},
                dispatchFunc = StubDispatchFunc(),
                message = CouplingSocketMessage("", emptyList())
            ) {}
        )
    } verify { wrapper ->
        wrapper.find(ServerMessage)
            .props()
            .tribeId
            .assertIsEqualTo(tribe.id)
    }

}

fun <T> T?.assertNotNull(callback: (T) -> Unit = {}) {
    this.assertIsNotEqualTo(null)
    callback(this!!)
}
