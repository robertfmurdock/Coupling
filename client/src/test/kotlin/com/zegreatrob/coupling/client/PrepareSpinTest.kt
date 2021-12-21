package com.zegreatrob.coupling.client

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.external.react.SimpleStyle
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.spin.prepareSpin
import com.zegreatrob.coupling.client.pairassignments.spin.StatefulPrepareSpin
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
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.findByClass
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PrepareSpinTest {

    companion object {
        val styles: SimpleStyle = useStyles("PrepareSpin")
    }

    @Test
    fun whenSelectedPinIsClickedWillDeselectPin() = setup(object {
        val tribe = stubTribe()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]
        val wrapper = shallow(StatefulPrepareSpin(tribe, players, null, pins, StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpin)
            .shallow()
            .findByClass(styles["deselectedPins"]).find(pinButton)
            .dataprops().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = setup(object {
        val tribe = stubTribe()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val wrapper = shallow(StatefulPrepareSpin(tribe, players, null, pins, StubDispatchFunc()))
    }) {
        wrapper.find(prepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } exercise {
        wrapper.find(prepareSpin)
            .shallow()
            .findByClass(styles["deselectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(prepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"]).find(pinButton)
            .at(0)
            .dataprops()
            .pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val currentPairs = null
    }) exercise {
        shallow(StatefulPrepareSpin(tribe, players, currentPairs, emptyList(), StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.find(prepareSpin)
            .shallow()
            .find(playerCard).map { it.dataprops().deselected.assertIsEqualTo(true) }
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val currentPairs = null
        val wrapper = shallow(StatefulPrepareSpin(tribe, players, currentPairs, emptyList(), StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpin)
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
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val currentPairs = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = players.map { pairOf(it).withPins(emptyList()) }
        )
        val wrapper = shallow(StatefulPrepareSpin(tribe, players, currentPairs, emptyList(), StubDispatchFunc()))
    }) exercise {
        wrapper.find(prepareSpin)
            .shallow()
            .find(couplingButton)
            .map { it.dataprops() }
            .find { it.className == styles["selectNoneButton"] }
            ?.onClick?.invoke()
    } verify {
        wrapper.find(prepareSpin)
            .shallow()
            .find(playerCard).map { it.dataprops().deselected.assertIsEqualTo(true) }
    }

    private fun ShallowWrapper<*>.findPinButtonPropsFor(targetPin: Pin) = find(pinButton)
        .map { it.dataprops() }
        .first { it.pin == targetPin }

}
