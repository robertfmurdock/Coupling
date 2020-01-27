package com.zegreatrob.coupling.client.pairassignments

import ShallowWrapper
import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import findComponent
import kotlinx.coroutines.withContext
import shallow
import stubPin
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.test.Test

class PairAssignmentsTest {

    val tribe = Tribe(TribeId("Party"))
    private val styles = loadStyles<PairAssignmentsStyles>("pairassignments/PairAssignments")

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs(): Unit = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
        val fellow = Player(id = "3", name = "fellow")
        val guy = Player(id = "2", name = "Guy")

        val rigby = Player(id = "1", name = "rigby")
        val nerd = Player(id = "4", name = "nerd")
        val pantsmaster = Player(id = "5", name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pairOf(
                    Player(
                        id = "0",
                        name = "Tom"
                    ), Player(id = "z", name = "Jerry")
                ),
                pairOf(fellow, guy)
            ).withPins()
        )
    }) exercise {
        shallow(PairAssignmentsProps(tribe, players, pairAssignments) {})
    } verify { wrapper ->
        wrapper.findComponent(PlayerRoster)
            .props()
            .players
            .assertIsEqualTo(
                listOf(
                    rigby,
                    nerd,
                    pantsmaster
                )
            )
    }

    @Test
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
        val players = listOf(
            Player(id = "1", name = "rigby"),
            Player(id = "2", name = "Guy"),
            Player(id = "3", name = "fellow"),
            Player(id = "4", name = "nerd"),
            Player(id = "5", name = "pantsmaster")
        )
    }) exercise {
        shallow(PairAssignmentsProps(tribe, players, null) {})
    } verify { wrapper ->
        wrapper.findComponent(PlayerRoster)
            .props()
            .players
            .assertIsEqualTo(players)
    }

    @Test
    fun onClickSaveWillUseCouplingToSaveAndRedirectToCurrentPairAssignmentsPage() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : PairAssignmentsRenderer,
                PropsClassProvider<PairAssignmentsProps> by provider() {
                override val pairAssignmentDocumentRepository get() = TODO("not implemented")
                override fun buildScope() = this@withContext
                val saveSpy = object : Spy<Json, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPairAssignmentDocument.save() {
                    saveSpy.spyFunction(document.toJson())
                }

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val pairAssignments = PairAssignmentDocument(
                    date = DateTime.now(),
                    pairs = emptyList()
                )
                val wrapper =
                    shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments, pathSetterSpy::spyFunction))
            }) {
                saveSpy.spyWillReturn(Promise.resolve(Unit))
                pathSetterSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find<Any>(".${styles.saveButton}")
                    .simulate("click")
            }
        } verifyAsync {
            saveSpy.spyReceivedValues.size
                .assertIsEqualTo(1)
            pathSetterSpy.spyReceivedValues
                .assertContains("/${tribe.id.value}/pairAssignments/current/")
        }
    }

    @Test
    fun onPlayerDropWillTakeTwoPlayersAndSwapTheirPlaces() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
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
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments) {})
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.findComponent(AssignedPair)
        pairs.at(0).props().pair.toPair().asArray().toList()
            .assertIsEqualTo(listOf(player1, player3))
        pairs.at(1).props().pair.toPair().asArray().toList()
            .assertIsEqualTo(listOf(player2, player4))
    }

    @Test
    fun onPinDropWillTakeMovePinFromOnePairToAnother() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
        val pin1 = stubPin()
        val pin2 = stubPin()
        val pair1 = pairOf(Player("1", name = "1"), Player("2", name = "2")).withPins(listOf(pin1))
        val pair2 = pairOf(Player("3", name = "3"), Player("4", name = "4")).withPins(listOf(pin2))
        val pairAssignments = PairAssignmentDocument(
            date = DateTime.now(),
            pairs = listOf(
                pair1,
                pair2
            )
        )
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments) {})
    }) exercise {
        pin1.dragTo(pair2, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.findComponent(AssignedPair)
        pairs.at(0).props().pair
            .assertIsEqualTo(pair1.copy(pins = emptyList()))
        pairs.at(1).props().pair
            .assertIsEqualTo(pair2.copy(pins = listOf(pin2, pin1)))
    }

    @Test
    fun onPlayerDropTheSwapWillNotLosePinAssignments() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
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
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments) {})
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.findComponent(AssignedPair)
        pairs.at(0).props().pair.pins
            .assertIsEqualTo(listOf(pin1))
        pairs.at(1).props().pair.pins
            .assertIsEqualTo(listOf(pin2))
    }

    @Test
    fun onPlayerDropWillNotSwapPlayersThatAreAlreadyPaired() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")

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
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments) {})
    }) exercise {
        player4.dragTo(player3, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.findComponent(AssignedPair)
        pairs.at(0).props().pair.toPair().asArray().toList()
            .assertIsEqualTo(listOf(player1, player2))
        pairs.at(1).props().pair.toPair().asArray().toList()
            .assertIsEqualTo(listOf(player3, player4))
    }

    private fun Player.dragTo(target: Player, wrapper: ShallowWrapper<PairAssignmentsRenderer>) {
        val allAssignedPairProps = wrapper.findComponent(AssignedPair).map { it.props() }

        val targetProps = allAssignedPairProps.find { it.pair.toPair().asArray().contains(target) }

        targetProps.assertIsNotEqualTo(null)

        targetProps?.run {
            swapCallback(id!!, pair.players.first { it.player == target }, pair)
        }
    }

    private fun Pin.dragTo(targetPair: PinnedCouplingPair, wrapper: ShallowWrapper<PairAssignmentsRenderer>) {
        val allAssignedPairProps = wrapper.findComponent(AssignedPair).map { it.props() }
        val targetPairProps = allAssignedPairProps.first { it.pair == targetPair }
        targetPairProps.pinMoveCallback(this, targetPair)
    }

    @Test
    fun passesDownTribeIdToServerMessage() = setup(object : PairAssignmentsRenderer,
        PropsClassProvider<PairAssignmentsProps> by provider() {
        override val pairAssignmentDocumentRepository get() = TODO("not implemented")
    }) exercise {
        shallow(PairAssignmentsProps(tribe, listOf(), null) {})
    } verify { wrapper ->
        wrapper.findComponent(ServerMessage)
            .props()
            .tribeId
            .assertIsEqualTo(tribe.id)
    }

}