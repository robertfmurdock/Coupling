package com.zegreatrob.coupling.client

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpin
import com.zegreatrob.coupling.components.pinButton
import com.zegreatrob.coupling.components.playerCard
import com.zegreatrob.coupling.components.spin.deselectedPinsClass
import com.zegreatrob.coupling.components.spin.prepareSpinContent
import com.zegreatrob.coupling.components.spin.selectedPinsClass
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.waitFor
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.userEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.findByClass
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PrepareSpinTest {

    private val user = userEvent.setup()

    @Test
    fun whenSelectedPinIsClickedWillDeselectPin() = setup(object {
        val party = stubParty()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]
        val wrapper = shallow(PrepareSpin(party, players, null, pins, StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("$selectedPinsClass")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("$deselectedPinsClass").find(pinButton)
            .dataprops().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = setup(object {
        val party = stubParty()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val wrapper = shallow(PrepareSpin(party, players, null, pins, StubDispatchFunc()))
    }) {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("$selectedPinsClass")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } exercise {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("$deselectedPinsClass")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("$selectedPinsClass").find(pinButton)
            .at(0)
            .dataprops()
            .pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = null
    }) exercise {
        shallow(PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.find(prepareSpinContent)
            .shallow()
            .find(playerCard).map { it.dataprops().deselected.assertIsEqualTo(true) }
    }

    @Test
    fun whenAllPlayersAreDeselectedSpinButtonWillBeDisabled() = asyncSetup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = PairAssignmentDocument(
            PairAssignmentDocumentId(""),
            DateTime.now(),
            listOf(
                pairOf(players[0], players[1]).withPins(emptySet()),
                pairOf(players[2]).withPins(emptySet())
            )
        )
        val result = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            json("wrapper" to MemoryRouter)
        )
    }) exercise {
        result.container.querySelectorAll("[data-player-id]")
            .asList()
            .map { it as? HTMLElement }
            .forEach { htmlElement ->
                if (htmlElement?.attributes?.get("data-selected")?.value == "true") {
                    user.click(htmlElement).await()
                }
            }
    } verify {
        waitFor {
            result.getByText("Spin!")
                .attributes["disabled"]
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = asyncSetup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = null
        val context = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            json("wrapper" to MemoryRouter)
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
        }.await()
    }

    @Test
    fun whenTheNoneButtonIsClickedAllPlayersBecomeDeselected() = asyncSetup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = players.map { pairOf(it).withPins(emptySet()) }
        )
        val context = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            json("wrapper" to MemoryRouter)
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
        }.await()
    }

    private fun ShallowWrapper<*>.findPinButtonPropsFor(targetPin: Pin) = find(pinButton)
        .map { it.dataprops() }
        .first { it.pin == targetPin }
}
