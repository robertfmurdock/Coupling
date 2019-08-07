package com.zegreatrob.coupling.client.pairassignments

import ShallowWrapper
import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.withPins
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import findComponent
import kotlinx.coroutines.withContext
import shallow
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.test.Test

class PairAssignmentsTest {

    val tribe = KtTribe(TribeId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs(): Unit = setup(object : PairAssignmentsBuilder {
        val fellow = Player(id = "3", name = "fellow")
        val guy = Player(id = "2", name = "Guy")

        val rigby = Player(id = "1", name = "rigby")
        val nerd = Player(id = "4", name = "nerd")
        val pantsmaster = Player(id = "5", name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        val pairAssignments = PairAssignmentDocument(
                date = DateTime.now(),
                pairs = listOf(
                        pairOf(Player(id = "0", name = "Tom"), Player(id = "z", name = "Jerry")),
                        pairOf(fellow, guy)
                ).withPins()
        )
    }) exercise {
        shallow(PairAssignmentsProps(tribe, players, pairAssignments, {}))
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
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = setup(object : PairAssignmentsBuilder {
        val players = listOf(
                Player(id = "1", name = "rigby"),
                Player(id = "2", name = "Guy"),
                Player(id = "3", name = "fellow"),
                Player(id = "4", name = "nerd"),
                Player(id = "5", name = "pantsmaster")
        )
    }) exercise {
        shallow(PairAssignmentsProps(tribe, players, null, {}))
    } verify { wrapper ->
        wrapper.findComponent(PlayerRoster)
                .props()
                .players
                .assertIsEqualTo(players)
    }

    @Test
    fun onClickSaveWillUseCouplingToSaveAndRedirectToCurrentPairAssignmentsPage() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : PairAssignmentsBuilder {
                override fun buildScope() = this@withContext
                val saveSpy = object : Spy<Json, Promise<Unit>> by SpyData() {}
                override suspend fun saveAsync(tribeId: TribeId, pairAssignmentDocument: PairAssignmentDocument) {
                    saveSpy.spyFunction(pairAssignmentDocument.toJson())
                }

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val pairAssignments = PairAssignmentDocument(
                        date = DateTime.now(),
                        pairs = emptyList()
                )
                val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments, pathSetterSpy::spyFunction))
            }) {
                saveSpy.spyWillReturn(Promise.resolve(Unit))
                pathSetterSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find<Any>("#save-button")
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
    fun onPlayerDropWillTakeTwoPlayersAndSwapTheirPlaces() = setup(object : PairAssignmentsBuilder {
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
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments, {}))
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.find<Any>(".pair")
        pairs.at(0).findComponent(DraggablePlayer)
                .map { it.props().pinnedPlayer.player }
                .toList()
                .assertIsEqualTo(listOf(player1, player3))
        pairs.at(1).findComponent(DraggablePlayer)
                .map { it.props().pinnedPlayer.player }
                .toList()
                .assertIsEqualTo(listOf(player2, player4))
    }


    @Test
    fun onPlayerDropWillNotSwapPlayersThatAreAlreadyPaired() = setup(object : PairAssignmentsBuilder {
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
        val wrapper = shallow(PairAssignmentsProps(tribe, emptyList(), pairAssignments, {}))
    }) exercise {
        player4.dragTo(player3, wrapper)
    } verify {
        wrapper.update()

        val pairs = wrapper.find<Any>(".pair")
        pairs.at(0).findComponent(DraggablePlayer)
                .map { it.props().pinnedPlayer.player }
                .toList()
                .assertIsEqualTo(listOf(player1, player2))
        pairs.at(1).findComponent(DraggablePlayer)
                .map { it.props().pinnedPlayer.player }
                .toList()
                .assertIsEqualTo(listOf(player3, player4))
    }

    private fun Player.dragTo(target: Player, wrapper: ShallowWrapper<PairAssignmentsBuilder>) {
        val allDraggablePlayerProps = wrapper.findComponent(DraggablePlayer)
                .map { it.props() }
        val targetDraggableProps = allDraggablePlayerProps
                .find { props -> props.pinnedPlayer.player == target }
        targetDraggableProps?.onPlayerDrop?.invoke(id!!)
    }

    @Test
    fun passesDownTribeIdToServerMessage() = setup(object : PairAssignmentsBuilder {
    }) exercise {
        shallow(PairAssignmentsProps(tribe, listOf(), null, {}))
    } verify { wrapper ->
        wrapper.findComponent(ServerMessage)
                .props()
                .tribeId
                .assertIsEqualTo(tribe.id)
    }

}