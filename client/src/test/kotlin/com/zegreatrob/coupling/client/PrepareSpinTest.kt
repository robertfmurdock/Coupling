package com.zegreatrob.coupling.client

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.CouplingButtonProps
import com.zegreatrob.coupling.client.external.react.SimpleStyle
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpin
import com.zegreatrob.coupling.client.pairassignments.spin.StatefulPrepareSpin
import com.zegreatrob.coupling.client.pairassignments.spin.StatefulPrepareSpinProps
import com.zegreatrob.coupling.client.pin.PinButton
import com.zegreatrob.coupling.client.pin.PinButtonProps
import com.zegreatrob.coupling.client.player.PlayerCard
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
        val wrapper = shallow(
            StatefulPrepareSpin,
            StatefulPrepareSpinProps(tribe, players, null, pins, StubDispatchFunc())
        )
    }) exercise {
        wrapper.find(PrepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(PrepareSpin)
            .shallow()
            .findByClass(styles["deselectedPins"]).find(PinButton)
            .props().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = setup(object {
        val tribe = stubTribe()
        val players = emptyList<Player>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val wrapper =
            shallow(StatefulPrepareSpin, StatefulPrepareSpinProps(tribe, players, null, pins, StubDispatchFunc()))
    }) {
        wrapper.find(PrepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } exercise {
        wrapper.find(PrepareSpin)
            .shallow()
            .findByClass(styles["deselectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.find(PrepareSpin)
            .shallow()
            .findByClass(styles["selectedPins"]).find(PinButton)
            .at(0)
            .props().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val currentPairs = null
    }) exercise {
        shallow(
            StatefulPrepareSpin,
            StatefulPrepareSpinProps(tribe, players, currentPairs, emptyList(), StubDispatchFunc())
        )
    } verify { wrapper ->
        wrapper.find(PrepareSpin)
            .shallow()
            .find(PlayerCard).map { it.props().deselected.assertIsEqualTo(true) }
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val currentPairs = null
        val wrapper = shallow(
            StatefulPrepareSpin, StatefulPrepareSpinProps(tribe, players, currentPairs, emptyList(), StubDispatchFunc())
        )
    }) exercise {
        wrapper.find(PrepareSpin)
            .shallow()
            .find(CouplingButton)
            .map(ShallowWrapper<CouplingButtonProps>::props)
            .find { it.className == styles["selectAllButton"] }
            ?.onClick?.invoke()
    } verify {
        wrapper.find(PlayerCard).map { it.props().deselected.assertIsEqualTo(false) }
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
        val wrapper = shallow(
            StatefulPrepareSpin, StatefulPrepareSpinProps(tribe, players, currentPairs, emptyList(), StubDispatchFunc())
        )
    }) exercise {
        wrapper.find(PrepareSpin)
            .shallow()
            .find(CouplingButton).map { it.props() }
            .find { it.className == styles["selectNoneButton"] }
            ?.onClick?.invoke()
    } verify {
        wrapper.find(PrepareSpin)
            .shallow()
            .find(PlayerCard).map { it.props().deselected.assertIsEqualTo(true) }
    }

    private fun ShallowWrapper<*>.findPinButtonPropsFor(targetPin: Pin) = find(PinButton)
        .map(ShallowWrapper<PinButtonProps>::props)
        .first { it.pin == targetPin }

}
