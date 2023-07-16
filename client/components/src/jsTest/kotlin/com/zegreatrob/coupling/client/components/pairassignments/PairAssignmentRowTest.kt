package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import kotlinx.datetime.Clock
import org.w3c.dom.Window
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PairAssignmentRowTest {

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        val party = PartyDetails(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(PairAssignmentDocumentId("RealId"), Clock.System.now(), emptyList())
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PairAssignmentRow.create(
                party = party,
                document = document,
                controls = Controls(stubDispatcher.func(), reloadSpy::spyFunction),
                windowFunctions = this,
            ),
            jso { wrapper = MemoryRouter },
        )
    } exercise {
        actor.click(screen.getByText("DELETE"))
        act { stubDispatcher.resultChannel.send(VoidResult.Accepted) }
    } verify {
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(DeletePairAssignmentsCommand(party.id, document.id)))
        reloadSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        val party = PartyDetails(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(
            PairAssignmentDocumentId("RealId"),
            Clock.System.now(),
            emptyList(),
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PairAssignmentRow.create(
                party = party,
                document = document,
                controls = Controls(stubDispatcher.func(), reloadSpy::spyFunction),
                windowFunctions = this,
            ),
            jso { wrapper = MemoryRouter },
        )
    } exercise {
        actor.click(screen.getByText("DELETE"))
    } verify {
        stubDispatcher.receivedActions.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.callCount.assertIsEqualTo(0)
    }
}
