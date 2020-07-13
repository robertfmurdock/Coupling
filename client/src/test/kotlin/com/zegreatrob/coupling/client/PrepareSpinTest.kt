package com.zegreatrob.coupling.client

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.external.ShallowWrapper
import com.zegreatrob.coupling.client.external.findByClass
import com.zegreatrob.coupling.client.external.react.SimpleStyle
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.shallow
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpin
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinProps
import com.zegreatrob.coupling.client.pin.PinButton
import com.zegreatrob.coupling.client.pin.PinButtonProps
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
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
        val history = emptyList<PairAssignmentDocument>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val wrapper = shallow(PrepareSpin, PrepareSpinProps(tribe, players, history, pins) {})
    }) exercise {
        wrapper.findByClass(styles["selectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.findByClass(styles["deselectedPins"]).find(PinButton)
            .props().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenDeselectedPinIsClickedWillSelectPin() = setup(object {
        val tribe = stubTribe()
        val players = emptyList<Player>()
        val history = emptyList<PairAssignmentDocument>()
        val pins = listOf(stubPin(), stubPin())
        val firstPin = pins[0]

        val wrapper = shallow(PrepareSpin, PrepareSpinProps(tribe, players, history, pins) {})

        init {
            wrapper.findByClass(styles["selectedPins"])
                .findPinButtonPropsFor(firstPin)
                .onClick()
        }
    }) exercise {
        wrapper.findByClass(styles["deselectedPins"])
            .findPinButtonPropsFor(firstPin)
            .onClick()
    } verify {
        wrapper.findByClass(styles["selectedPins"]).find(PinButton)
            .at(0)
            .props().pin
            .assertIsEqualTo(firstPin)
    }

    @Test
    fun whenThereIsNoHistoryAllPlayersWillDefaultToDeselected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val history = emptyList<PairAssignmentDocument>()
    }) exercise {
        shallow(PrepareSpin, PrepareSpinProps(tribe, players, history, emptyList()) {})
    } verify { wrapper ->
        wrapper.find(PlayerCard).map { it.props().deselected.assertIsEqualTo(true) }
    }

    @Test
    fun whenTheAllButtonIsClickedAllPlayersBecomeSelected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val history = emptyList<PairAssignmentDocument>()
        val wrapper = shallow(PrepareSpin, PrepareSpinProps(tribe, players, history, emptyList()) {})
    }) exercise {
        wrapper.findByClass(styles["selectAllButton"]).simulate("click")
    } verify {
        wrapper.find(PlayerCard).map { it.props().deselected.assertIsEqualTo(false) }
    }

    @Test
    fun whenTheNoneButtonIsClickedAllPlayersBecomeDeselected() = setup(object {
        val tribe = stubTribe()
        val players = stubPlayers(3)
        val history = listOf(
            PairAssignmentDocument(date = DateTime.now(), pairs = players.map { pairOf(it).withPins(emptyList()) })
        )
        val wrapper = shallow(PrepareSpin, PrepareSpinProps(tribe, players, history, emptyList()) {})
    }) exercise {
        wrapper.findByClass(styles["selectNoneButton"]).simulate("click")
    } verify {
        wrapper.find(PlayerCard).map { it.props().deselected.assertIsEqualTo(true) }
    }

    private fun ShallowWrapper<Any>.findPinButtonPropsFor(targetPin: Pin) = find(PinButton)
        .map(ShallowWrapper<PinButtonProps>::props)
        .first { it.pin == targetPin }

}
