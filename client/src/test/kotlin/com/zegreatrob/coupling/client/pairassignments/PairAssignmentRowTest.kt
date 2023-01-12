package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.list.PairAssignmentRow
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.userEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import org.w3c.dom.Window
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PairAssignmentRowTest {

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        val party = Party(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(PairAssignmentDocumentId("RealId"), DateTime.now(), emptyList())
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            PairAssignmentRow(party, document, Controls(stubDispatcher.func(), reloadSpy::spyFunction), this).create {},
            json("wrapper" to MemoryRouter)
        )
    } exercise {
        actor.click(screen.getByText("DELETE")).await()

        stubDispatcher.simulateSuccess<DeletePairAssignmentsCommand>()
    } verify {
        stubDispatcher.commandsDispatched<DeletePairAssignmentsCommand>()
            .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(party.id, document.id)))
        reloadSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        val party = Party(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(
            PairAssignmentDocumentId("RealId"),
            DateTime.now(),
            emptyList()
        )
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            PairAssignmentRow(party, document, Controls(stubDispatcher.func(), reloadSpy::spyFunction), this).create {},
            json("wrapper" to MemoryRouter)
        )
    } exercise {
        actor.click(screen.getByText("DELETE")).await()
    } verify {
        stubDispatcher.dispatchList.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.callCount.assertIsEqualTo(0)
    }
}
