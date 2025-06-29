package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.components.spin.deselectedPinsClass
import com.zegreatrob.coupling.client.components.spin.selectedPinsClass
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.reactTestingLibrary
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.unsafeJso
import kotools.types.collection.notEmptyListOf
import kotools.types.collection.toNotEmptyList
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.test.Test
import kotlin.time.Clock

class PrepareSpinTest {

    private val prepareSetup = asyncTestTemplate(
        sharedSetup = suspend {
            val matchFunc = js.globals.globalThis["window"].asDynamic().matchMedia
            js.globals.globalThis["window"].asDynamic()["matchMedia"] = fun() {}
            object {
                val matchFunc = matchFunc
            }
        },
        sharedTeardown = {
            js.globals.globalThis["window"].asDynamic()["matchMedia"] = it.matchFunc
        },
    )

    @Test
    fun whenSelectedPinIsClickedWillDeselectPin() = prepareSetup(object {
        val user = UserEvent.setup()
        val party = stubPartyDetails()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]
        val wrapper = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, null, pins, { {} })
        }
    }) exercise {
        user.click(
            wrapper.container.querySelector(".$selectedPinsClass")
                ?.querySelectorAll("[data-pin-button=\"${firstPin.id.value}\"]")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.firstOrNull(),
        )
    } verify {
        wrapper.container.querySelector(".$deselectedPinsClass")
            ?.querySelectorAll("[data-pin-button='${firstPin.id.value}']")
            ?.asList()
            ?.map { it as? HTMLElement }
            ?.firstOrNull()
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenMultipleSelectedPinsAreClickedWillDeselectAll() = prepareSetup(object {
        val user = UserEvent.setup()
        val party = stubPartyDetails()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]
        val wrapper = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, null, pins, { {} })
        }
    }) exercise {
        wrapper.container.querySelector(".$selectedPinsClass")
            ?.querySelectorAll("[data-pin-button=\"${firstPin.id.value}\"]")
            ?.asList()
            ?.map { it as? HTMLElement }
            ?.forEach { user.click(it) }
    } verify {
        reactTestingLibrary.waitFor {
            wrapper.container.querySelector(".$deselectedPinsClass")
                ?.querySelectorAll("[data-pin-button='${firstPin.id.value}']")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.size
                .assertIsEqualTo(pins.size)
        }
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = prepareSetup(object {
        val user = UserEvent.setup()
        val party = stubPartyDetails()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val render = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, null, pins, { {} })
        }
    }) {
        user.click(
            render.container.querySelector(".$selectedPinsClass")
                ?.querySelectorAll("[data-pin-button=\"${firstPin.id.value}\"]")
                ?.asList()
                ?.firstNotNullOf { it as? HTMLElement }!!,
        )
    } exercise {
        user.click(
            render.container.querySelector(".$deselectedPinsClass")
                ?.querySelectorAll("[data-pin-button='${firstPin.id.value}']")
                ?.asList()
                ?.map { it as? HTMLElement }
                ?.first(),
        )
    } verify {
        render.container.querySelector(".$selectedPinsClass")
            ?.querySelectorAll("[data-pin-button='${firstPin.id.value}']")
            ?.asList()
            ?.map { it as? HTMLElement }
            ?.firstOrNull()
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val currentPairs = null
    }) exercise {
        render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, currentPairs, emptyList(), { {} })
        }
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
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val user = UserEvent.setup()
        val currentPairs = PairAssignmentDocument(
            PairAssignmentDocumentId.new(),
            Clock.System.now(),
            notEmptyListOf(
                pairOf(players[0], players[1]).withPins(emptySet()),
                pairOf(players[2]).withPins(emptySet()),
            ),
            null,
        )
        val result = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, currentPairs, emptyList(), { {} })
        }
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
        screen.getByText("Spin!")
            .attributes["disabled"]
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val user = UserEvent.setup()
        val currentPairs = null
        val context = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players, currentPairs, emptyList(), { {} })
        }
    }) exercise {
        user.click(screen.getByText("All in!"))
    } verify {
        context.baseElement.querySelectorAll("[data-player-id]")
            .asList()
            .mapNotNull { it as? HTMLElement }
            .map { it.getAttribute("data-selected") }
            .assertIsEqualTo(listOf("true", "true", "true"))
    }

    @Test
    fun whenTheNoneButtonIsClickedAllPlayersBecomeDeselected() = asyncSetup(object {
        val party = stubPartyDetails()
        val user = UserEvent.setup()
        val players = stubPlayers(3).toNotEmptyList().getOrThrow()
        val currentPairs = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = Clock.System.now(),
            pairs = players.map { pairOf(it).withPins(emptySet()) },
            null,
        )
        val context = render(unsafeJso { wrapper = TestRouter }) {
            PrepareSpin(party, players.toList(), currentPairs, emptyList(), { {} })
        }
    }) exercise {
        user.click(screen.getByText("All out!"))
    } verify {
        context.baseElement.querySelectorAll("[data-player-id]")
            .asList()
            .mapNotNull { it as? HTMLElement }
            .map { it.getAttribute("data-selected") }
            .assertIsEqualTo(listOf("false", "false", "false"))
    }
}
