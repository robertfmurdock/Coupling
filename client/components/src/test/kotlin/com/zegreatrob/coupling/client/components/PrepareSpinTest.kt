package com.zegreatrob.coupling.client.components

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.components.spin.deselectedPinsClass
import com.zegreatrob.coupling.client.components.spin.selectedPinsClass
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import korlibs.time.DateTime
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import react.router.MemoryRouter
import web.window.window
import kotlin.test.Test

class PrepareSpinTest {

    private val prepareSetup = asyncTestTemplate(
        sharedSetup = suspend {
            val matchFunc = window.asDynamic()["matchMedia"]
            window.asDynamic()["matchMedia"] = {}
            object {
                val matchFunc = matchFunc
            }
        },
        sharedTeardown = {
            window.asDynamic()["matchMedia"] = it.matchFunc
        },
    )

    @Test
    fun whenSelectedPinIsClickedWillDeselectPin() = prepareSetup(object {
        val user = UserEvent.setup()
        val party = stubParty()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]
        val wrapper = render(
            PrepareSpin(party, players, null, pins, StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    }) exercise {
        user.click(
            wrapper.container.querySelector(".$selectedPinsClass")
                ?.querySelectorAll("[data-pin-button=\"${firstPin.id}\"]")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.firstOrNull(),
        )
    } verify {
        waitFor {
            wrapper.container.querySelector(".$deselectedPinsClass")
                ?.querySelectorAll("[data-pin-button='${firstPin.id}']")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.firstOrNull()
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = prepareSetup(object {
        val user = UserEvent.setup()
        val party = stubParty()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val render = render(
            PrepareSpin(party, players, null, pins, StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    }) {
        user.click(
            render.container.querySelector(".$selectedPinsClass")
                ?.querySelectorAll("[data-pin-button=\"${firstPin.id}\"]")
                ?.asList()
                ?.firstNotNullOf { it as? HTMLElement }!!,
        )
    } exercise {
        user.click(
            render.container.querySelector(".$deselectedPinsClass")
                ?.querySelectorAll("[data-pin-button='${firstPin.id}']")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.first(),
        )
    } verify {
        waitFor {
            render.container.querySelector(".$selectedPinsClass")
                ?.querySelectorAll("[data-pin-button='${firstPin.id}']")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.firstOrNull()
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = null
    }) exercise {
        render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    } verify { result ->
        result.container.querySelectorAll("[data-player-id]")
            .asList()
            .map { it as? HTMLElement }
            .forEach { htmlElement ->
                htmlElement
                    ?.attributes
                    ?.get("data-selected")
                    ?.value
                    .assertIsEqualTo("false")
            }
    }

    @Test
    fun whenAllPlayersAreDeselectedSpinButtonWillBeDisabled() = asyncSetup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val user = UserEvent.setup()
        val currentPairs = PairAssignmentDocument(
            PairAssignmentDocumentId(""),
            DateTime.now(),
            listOf(
                pairOf(players[0], players[1]).withPins(emptySet()),
                pairOf(players[2]).withPins(emptySet()),
            ),
        )
        val result = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    }) exercise {
        result.container.querySelectorAll("[data-player-id]")
            .asList()
            .map { it as? HTMLElement }
            .forEach { htmlElement ->
                if (htmlElement?.attributes?.get("data-selected")?.value == "true") {
                    user.click(htmlElement)
                }
            }
    } verify {
        waitFor {
            screen.getByText("Spin!")
                .attributes["disabled"]
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = asyncSetup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val user = UserEvent.setup()
        val currentPairs = null
        val context = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    }) exercise {
        user.click(screen.getByText("All in!"))
    } verify {
        waitFor {
            context.baseElement.querySelectorAll("[data-player-id]")
                .asList()
                .mapNotNull { it as? HTMLElement }
                .map { it.getAttribute("data-selected") }
                .assertIsEqualTo(listOf("true", "true", "true"))
        }
    }

    @Test
    fun whenTheNoneButtonIsClickedAllPlayersBecomeDeselected() = asyncSetup(object {
        val party = stubParty()
        val user = UserEvent.setup()
        val players = stubPlayers(3)
        val currentPairs = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = players.map { pairOf(it).withPins(emptySet()) },
        )
        val context = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            jso { wrapper = MemoryRouter },
        )
    }) {
    } exercise {
        user.click(screen.getByText("All out!"))
    } verify {
        waitFor {
            context.baseElement.querySelectorAll("[data-player-id]")
                .asList()
                .mapNotNull { it as? HTMLElement }
                .map { it.getAttribute("data-selected") }
                .assertIsEqualTo(listOf("false", "false", "false"))
        }
    }
}
