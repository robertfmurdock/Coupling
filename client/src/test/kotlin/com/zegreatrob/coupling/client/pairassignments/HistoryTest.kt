package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryComponent
import com.zegreatrob.coupling.client.pairassignments.list.HistoryProps
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.setup
import org.w3c.dom.Window
import shallow
import kotlin.js.json
import kotlin.test.Test

class HistoryTest {

    private val styles = useStyles("pairassignments/History")

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = setup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

        val tribe = Tribe(TribeId("me"))

        val reloadSpy = SpyData<Unit, Unit>()

        val history = listOf(
            PairAssignmentDocument(
                PairAssignmentDocumentId("RealId"),
                DateTime.now(),
                emptyList()
            )
        )
        val stubDispatchFunc = StubDispatchFunc<DeletePairAssignmentsCommandDispatcher>()
        val wrapper = shallow(
            HistoryComponent(this),
            HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {}, stubDispatchFunc)
        )
    }, {
        reloadSpy.spyWillReturn(Unit)
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}").simulate("click")
        stubDispatchFunc.simulateSuccess<DeletePairAssignmentsCommand>()
    } verify {
        stubDispatchFunc.commandsDispatched<DeletePairAssignmentsCommand>()
            .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(tribe.id, history[0].id!!)))
        reloadSpy.spyReceivedValues.isNotEmpty()
            .assertIsEqualTo(true)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = setup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()

        val tribe = Tribe(TribeId("me"))

        val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

        val history = listOf(
            PairAssignmentDocument(
                PairAssignmentDocumentId("RealId"),
                DateTime.now(),
                emptyList()
            )
        )
        val stubDispatchFunc = StubDispatchFunc<DeletePairAssignmentsCommandDispatcher>()
        val wrapper = shallow(
            HistoryComponent(this),
            HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {}, stubDispatchFunc)
        )
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}").simulate("click")
    } verify {
        stubDispatchFunc.dispatchList.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.spyReceivedValues.isEmpty()
            .assertIsEqualTo(true)
    }
}
