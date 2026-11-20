package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.external.reactdnd.dndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5Backend
import com.zegreatrob.coupling.client.components.userEvent
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPinnedCouplingPair
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.nfc
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.unsafeJso
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotools.types.collection.notEmptyListOf
import react.Props
import react.ReactNode
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter
import react.useState
import kotlin.test.Test
import kotlin.time.Clock

class CurrentPairAssignmentsPanelTest {

    private class SwapBoard(withPin: Boolean = false, withPartner: Boolean = false, pinName: String = "Facilitator") {
        val alex = stubPlayer().copy(name = "Alex")
        val sam = stubPlayer().copy(name = "Sam")
        val pin = stubPin().copy(name = pinName)
        val pins = if (withPin) setOf(pin) else emptySet()
        val firstPair = if (withPartner) pairOf(alex, stubPlayer().copy(name = "Jordan")) else pairOf(alex)
        val pairs = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(firstPair.withPins(pins), pairOf(sam).withPins()))
        val changes = mutableListOf<PairingSet>()
        val actor = userEvent.setup()

        private val board by nfc<Props> {
            val (current, update) = useState(pairs)
            dndProvider {
                backend = html5Backend
                CurrentPairAssignmentsPanel(
                    party = party,
                    pairAssignments = current,
                    setPairAssignments = {
                        changes.add(it)
                        update(it)
                    },
                    allowSave = true,
                    dispatchFunc = dispatcher.func(),
                )
            }
        }

        private val party = stubPartyDetails()
        private val dispatcher = StubDispatcher()

        fun renderBoard() = render { board {} }

        fun player(name: String) = screen.getByRole("button", RoleOptions(name = name))
    }

    @Test
    fun keyboardActivationSwapsPlayersAcrossPairs() = asyncSetup(SwapBoard()) {
        renderBoard()
    } exercise {
        actor.tab().await()
        document.activeElement.assertIsEqualTo(player("Alex"))
        actor.keyboard("{Enter}").await()
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Swap with…")))
        actor.keyboard(" ").await()
        actor.tab().await()
        document.activeElement.assertIsEqualTo(player("Sam"))
        actor.keyboard("{Enter}").await()
    } verify {
        changes.single().pairs.toList().map { it.pinnedPlayers.toList().single().player }.assertIsEqualTo(listOf(sam, alex))
        document.activeElement.assertIsEqualTo(player("Alex"))
        screen.getByRole("status").textContent.assertIsEqualTo("Alex and Sam swapped.")
    }

    @Test
    fun completedSwapConfirmationClearsWithoutTakingFocus() = asyncSetup(SwapBoard()) {
        renderBoard()
        actor.click(player("Alex")).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Swap with…"))).await()
        actor.click(player("Sam")).await()
        screen.getByRole("status").textContent.assertIsEqualTo("Alex and Sam swapped.")
    } exercise {
        delay(5_500)
    } verify {
        screen.getByRole("status").textContent.assertIsEqualTo("")
        document.activeElement.assertIsEqualTo(player("Alex"))
    }

    @Test
    fun selectingASamePairPlayerAndPressingEscapeLeavesAssignmentsIntact() = asyncSetup(SwapBoard(withPartner = true)) {
        renderBoard()
        actor.click(player("Alex")).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Swap with…"))).await()
    } exercise {
        actor.click(player("Jordan")).await()
        actor.keyboard("{Escape}").await()
    } verify {
        changes.isEmpty().assertIsEqualTo(true)
        document.activeElement.assertIsEqualTo(player("Alex"))
        screen.getByRole("status").textContent.assertIsEqualTo("")
    }

    @Test
    fun keyboardSelectionMovesAnAssignedPinToTheChosenPair() = asyncSetup(SwapBoard(withPin = true)) {
        renderBoard()
        actor.tab().await()
        actor.tab().await()
    } exercise {
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Facilitator")))
        actor.keyboard("{Enter}").await()
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Move to…")))
        actor.keyboard("{Enter}").await()
        actor.tab().await()
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Move Facilitator to Sam")))
        actor.keyboard(" ").await()
    } verify {
        changes.single().pairs.toList().map { it.pins }.assertIsEqualTo(listOf(emptySet(), setOf(pin)))
        screen.getByRole("status").textContent.assertIsEqualTo("Facilitator moved to Sam.")
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Facilitator")))
    }

    @Test
    fun activatingTheSourcePinAgainCancelsTheMove() = asyncSetup(SwapBoard(withPin = true)) {
        renderBoard()
        actor.click(screen.getByRole("button", RoleOptions(name = "Facilitator"))).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Move to…"))).await()
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Facilitator"))).await()
    } verify {
        changes.isEmpty().assertIsEqualTo(true)
        screen.queryByRole("button", RoleOptions(name = "Move Facilitator to Sam")).assertIsEqualTo(null)
    }

    @Test
    fun anUnnamedPinHasAnAccessibleActionAndDestination() = asyncSetup(SwapBoard(withPin = true, pinName = "")) {
        renderBoard()
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Unnamed pin"))).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Move to…"))).await()
    } verify {
        screen.getByRole("button", RoleOptions(name = "Move Unnamed pin to Sam"))
    }

    @Test
    fun clickingSaveButtonWillNRedirectToCurrentPairAssignmentsPageWithoutSavingBecauseAutosave() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = PairingSet(
            id = PairingSetId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(stubPinnedCouplingPair()),
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        unsafeJso {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("current pairs")
                        },
                        unsafeJso {
                            path = "*"
                            element = CurrentPairAssignmentsPanel.create(
                                party,
                                pairAssignments,
                                setPairAssignments = { },
                                allowSave = true,
                                dispatchFunc = stubDispatcher.func(),
                            )
                        },
                    ),
                )
            },
        )
    } exercise {
        actor.click(screen.getByText("Save!"))
    } verify {
        stubDispatcher.receivedActions.size
            .assertIsEqualTo(0)
        screen.getByText("current pairs")
    }

    @Test
    fun clickingDeleteButtonWillPerformDeleteCommandAndReload() = asyncSetup(object {
        val party = stubPartyDetails()
        val pairAssignments = stubPairAssignmentDoc()
        val stubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
    }) {
        render {
            dndProvider {
                backend = html5Backend
                RouterProvider {
                    router = createMemoryRouter(
                        arrayOf(
                            unsafeJso {
                                path = "/${party.id.value}/pairAssignments/current/"
                                element = ReactNode("current pairs")
                            },
                            unsafeJso {
                                path = "*"
                                element = CurrentPairAssignmentsPanel.create(
                                    party,
                                    pairAssignments,
                                    setPairAssignments = { },
                                    allowSave = true,
                                    dispatchFunc = stubDispatcher.func(),
                                )
                            },
                        ),
                    )
                }
            }
        }
    } exercise {
        actor.click(screen.findByText("Cancel"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { receivedAction ->
        receivedAction
            .assertIsEqualTo(DeletePairAssignmentsCommand(party.id, pairAssignments.id))
        screen.getByText("current pairs")
    }
}
