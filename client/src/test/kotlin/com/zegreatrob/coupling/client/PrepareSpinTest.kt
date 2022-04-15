package com.zegreatrob.coupling.client

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.external.react.SimpleStyle
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.testinglibrary.react.render
import com.zegreatrob.coupling.client.external.testinglibrary.userevent.userEvent
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpin
import com.zegreatrob.coupling.client.pairassignments.spin.prepareSpinContent
import com.zegreatrob.coupling.client.pin.pinButton
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.findByClass
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import kotlinx.dom.hasClass
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PrepareSpinTest {

    val user = userEvent.setup()

    companion object {
        val styles: SimpleStyle = useStyles("PrepareSpin")
        val playerCardStyles = useStyles("player/PlayerCard")
    }

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
            .findByClass("${styles["selectedPins"]}")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("${styles["deselectedPins"]}").find(pinButton)
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
            .findByClass("${styles["selectedPins"]}")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } exercise {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("${styles["deselectedPins"]}")
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpinContent)
            .shallow()
            .findByClass("${styles["selectedPins"]}").find(pinButton)
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
            DateTime.now(), listOf(
                pairOf(players[0], players[1]).withPins(emptyList()),
                pairOf(players[2]).withPins(emptyList())
            )
        )
        val result = render(
            PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()).create(),
            json("wrapper" to MemoryRouter)
        )
    }) exercise {
        val playerCards = result.container.querySelectorAll(".${playerCardStyles["player"]}").asList()
        playerCards.forEach {
            if ((it as? HTMLElement)?.hasClass("${playerCardStyles["deselected"]}") == false) {
                user.click(it).await()
            }
        }
    } verify {
        result.getByText("Spin!")
            .attributes["disabled"]
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = setup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = null
        val wrapper = shallow(PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpinContent)
            .shallow()
            .find(couplingButton)
            .map { it.dataprops() }
            .find { it.className == styles["selectAllButton"] }
            ?.onClick?.invoke()
    } verify {
        wrapper.find(playerCard).map { it.dataprops().deselected.assertIsEqualTo(false) }
    }

    @Test
    fun whenTheNoneButtonIsClickedAllPlayersBecomeDeselected() = setup(object {
        val party = stubParty()
        val players = stubPlayers(3)
        val currentPairs = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = players.map { pairOf(it).withPins(emptyList()) }
        )
        val wrapper = shallow(PrepareSpin(party, players, currentPairs, emptyList(), StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpinContent)
            .shallow()
            .find(couplingButton)
            .map { it.dataprops() }
            .find { it.className == styles["selectNoneButton"] }
            ?.onClick?.invoke()
    } verify {
        wrapper.find(prepareSpinContent)
            .shallow()
            .find(playerCard).map { it.dataprops().deselected.assertIsEqualTo(true) }
    }

    private fun ShallowWrapper<*>.findPinButtonPropsFor(targetPin: Pin) = find(pinButton)
        .map { it.dataprops() }
        .first { it.pin == targetPin }

}
