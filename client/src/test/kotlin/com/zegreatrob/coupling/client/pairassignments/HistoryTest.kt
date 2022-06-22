package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.list.History
import com.zegreatrob.coupling.client.pairassignments.list.historyFunc
import com.zegreatrob.coupling.components.couplingButton
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import org.w3c.dom.Window
import kotlin.js.json
import kotlin.test.Test

class HistoryTest {

    private val styles = useStyles("pairassignments/History")

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = setup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        val party = Party(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val history = listOf(PairAssignmentDocument(PairAssignmentDocumentId("RealId"), DateTime.now(), emptyList()))
        val stubDispatcher = StubDispatcher()

        val wrapper = shallow(
            History(party, history, Controls(stubDispatcher.func(), reloadSpy::spyFunction)),
            historyFunc(this),
        )
    }) exercise {
        wrapper.find(couplingButton).map { it.dataprops() }.find { it.className == styles["deleteButton"] }
            ?.onClick?.invoke()

        stubDispatcher.simulateSuccess<DeletePairAssignmentsCommand>()
    } verify {
        stubDispatcher.commandsDispatched<DeletePairAssignmentsCommand>()
            .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(party.id, history[0].id)))
        reloadSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = setup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        val party = Party(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val history = listOf(
            PairAssignmentDocument(
                PairAssignmentDocumentId("RealId"),
                DateTime.now(),
                emptyList()
            )
        )
        val stubDispatcher = StubDispatcher()
        val wrapper = shallow(
            History(party, history, Controls(stubDispatcher.func(), reloadSpy::spyFunction)),
            historyFunc(this),
        )
    }) exercise {
        wrapper.find(couplingButton).map { it.dataprops() }.find { it.className == styles["deleteButton"] }
            ?.onClick?.invoke()
    } verify {
        stubDispatcher.dispatchList.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.callCount.assertIsEqualTo(0)
    }
}
