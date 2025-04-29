package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubPinnedCouplingPair
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.unsafeJso
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.Window
import kotlin.js.json
import kotlin.test.Test

class PairAssignmentRowTest {

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        val party = PartyDetails(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(stubPinnedCouplingPair()),
        )
        val stubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
    }) {
        render(
            PairAssignmentRow.create(
                party = party,
                document = document,
                controls = Controls(stubDispatcher.func(), reloadSpy::spyFunction),
                windowFunctions = this,
            ),
            unsafeJso { wrapper = TestRouter },
        )
    } exercise {
        actor.click(screen.getByText("DELETE"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(DeletePairAssignmentsCommand(party.id, document.id))
        reloadSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = asyncSetup(object : WindowFunctions {
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        val party = PartyDetails(PartyId("me"))
        val reloadSpy = SpyData<Unit, Unit>()
        val document = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(stubPinnedCouplingPair()),
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
            unsafeJso { wrapper = TestRouter },
        )
    } exercise {
        actor.click(screen.getByText("DELETE"))
    } verify {
        stubDispatcher.receivedActions.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.callCount.assertIsEqualTo(0)
    }
}
