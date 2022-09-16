package com.zegreatrob.coupling.client.pairassignments

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.waitFor
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.userEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import react.ReactNode
import react.create
import react.router.MemoryRouter
import react.router.Route
import react.router.Routes
import kotlin.test.Test

class CurrentPairAssignmentsPanelTest {

    @Test
    fun clickingSaveButtonWillNRedirectToCurrentPairAssignmentsPageWithoutSavingBecauseAutosave() = asyncSetup(object {
        val party = stubParty()
        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(), pairs = emptyList()
        )
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            MemoryRouter.create {
                Routes {
                    Route {
                        path = "/${party.id.value}/pairAssignments/current/"
                        element = ReactNode("current pairs")
                    }
                    Route {
                        path = "*"
                        element = CurrentPairAssignmentsPanel(
                            party,
                            pairAssignments,
                            setPairAssignments = { },
                            allowSave = true,
                            dispatchFunc = stubDispatcher.func()
                        ).create()
                    }
                }
            }
        )
    } exercise {
        actor.click(screen.getByText("Save!")).await()
        stubDispatcher.simulateSuccess<SavePairAssignmentsCommand>()
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<SavePairAssignmentsCommand>().size
                .assertIsEqualTo(0)
            screen.getByText("current pairs")
        }.await()
    }

    @Test
    fun clickingDeleteButtonWillPerformDeleteCommandAndReload() = asyncSetup(object {
        val party = stubParty()
        val pairAssignments = stubPairAssignmentDoc()
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            DndProvider.create {
                backend = HTML5Backend
                MemoryRouter {
                    Routes {
                        Route {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("current pairs")
                        }
                        Route {
                            path = "*"
                            element = CurrentPairAssignmentsPanel(
                                party,
                                pairAssignments,
                                setPairAssignments = { },
                                allowSave = true,
                                dispatchFunc = stubDispatcher.func()
                            ).create()
                        }
                    }
                }
            }
        )
    } exercise {
        actor.click(screen.getByText("Cancel")).await()
        stubDispatcher.simulateSuccess<DeletePairAssignmentsCommand>()
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<DeletePairAssignmentsCommand>()
                .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(party.id, pairAssignments.id)))
            screen.getByText("current pairs")
        }.await()
    }

    @Test
    fun onPlayerDropWillTakeTwoPlayersAndSwapTheirPlaces() = setup(object {
        val party = stubParty()
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2),
                pairOf(player3, player4)
            ).withPins()
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            CurrentPairAssignmentsPanel(
                party,
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                allowSave = false
            )
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
        val party = stubParty()
        val pin1 = stubPin()
        val pin2 = stubPin()
        val pair1 = pairOf(Player("1", name = "1"), Player("2", name = "2")).withPins(setOf(pin1))
        val pair2 = pairOf(Player("3", name = "3"), Player("4", name = "4")).withPins(setOf(pin2))
        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(),
            pairs = listOf(pair1, pair2)
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            CurrentPairAssignmentsPanel(
                party,
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                allowSave = false
            )
        )
    }) exercise {
        pin1.dragTo(pair2, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull { pairs ->
            pairs.pairs[0]
                .assertIsEqualTo(pair1.copy(pins = emptySet()))
            pairs.pairs[1]
                .assertIsEqualTo(pair2.copy(pins = setOf(pin2, pin1)))
        }
    }

    @Test
    fun onPlayerDropTheSwapWillNotLosePinAssignments() = setup(object {
        val party = stubParty()
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pin1 = stubPin()
        val pin2 = stubPin()

        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2).withPins(setOf(pin1)),
                pairOf(player3, player4).withPins(setOf(pin2))
            )
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            CurrentPairAssignmentsPanel(
                party,
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                allowSave = false
            )
        )
    }) exercise {
        player2.dragTo(player3, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull {
            it.pairs[0].pins
                .assertIsEqualTo(setOf(pin1))
            it.pairs[1].pins
                .assertIsEqualTo(setOf(pin2))
        }
    }

    @Test
    fun onPlayerDropWillNotSwapPlayersThatAreAlreadyPaired() = setup(object {
        val party = stubParty()
        val player1 = Player("1", name = "1")
        val player2 = Player("2", name = "2")
        val player3 = Player("3", name = "3")
        val player4 = Player("4", name = "4")

        val pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(),
            pairs = listOf(
                pairOf(player1, player2),
                pairOf(player3, player4)
            ).withPins()
        )
        var lastSetPairAssignments: PairAssignmentDocument? = null
        val wrapper = shallow(
            CurrentPairAssignmentsPanel(
                party,
                pairAssignments,
                { lastSetPairAssignments = it },
                dispatchFunc = StubDispatchFunc(),
                allowSave = false
            )
        )
    }) exercise {
        player4.dragTo(player3, wrapper)
    } verify {
        lastSetPairAssignments.assertNotNull {
            it.pairs[0].toPair().assertIsEqualTo(pairOf(player1, player2))
            it.pairs[1].toPair().assertIsEqualTo(pairOf(player3, player4))
        }
    }

    private fun Player.dragTo(target: Player, wrapper: ShallowWrapper<TMFC>) {
        val targetPairProps = wrapper.find(assignedPair)
            .map { it.dataprops<AssignedPair>() }
            .first { props -> props.pair.players.map { it.player }.contains(target) }
        val pair = targetPairProps.pair
        val swapCallback = targetPairProps.swapPlayersFunc
        swapCallback.invoke(pair.players.first { it.player == target }, id)
    }

    private fun Pin.dragTo(targetPair: PinnedCouplingPair, wrapper: ShallowWrapper<TMFC>) {
        val targetPairProps = wrapper.find(assignedPair)
            .map { it.dataprops<AssignedPair>() }
            .first { it.pair == targetPair }
        targetPairProps.pinDropFunc.invoke(this.id!!)
    }
}

fun <T> T?.assertNotNull(callback: (T) -> Unit = {}) {
    this.assertIsNotEqualTo(null)
    callback(this!!)
}
