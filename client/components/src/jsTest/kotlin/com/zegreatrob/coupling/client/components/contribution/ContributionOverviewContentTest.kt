package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.MemoryRouter
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.components.userEvent
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ContributionOverviewContentTest {

    @Test
    fun creatingParticipantReturnsFocusAfterItsActionDisappears() = asyncSetup(object {
        val expectedParty = stubPartyDetails()
        val expectedContribution = stubContribution().copy(
            participantEmails = setOf("alex@example.invalid", "sam@example.invalid"),
            link = null,
        )
        val dispatcher = StubDispatcher.Channel()
        val actor = userEvent.setup()
        lateinit var originalTrigger: HTMLElement
    }) {
        render(RenderOptions(wrapper = MemoryRouter)) {
            UpdatingPlayerList(
                players = emptyList(),
                dispatchFunc = dispatcher.func<SavePlayerCommand.Dispatcher>(),
            ) { updatedPlayers, updatedDispatcher ->
                ContributionOverviewContent(
                    party = expectedParty,
                    contributions = listOf(expectedContribution),
                    players = updatedPlayers,
                    dispatchFunc = updatedDispatcher,
                )
            }
        }
        originalTrigger = screen.getByRole("button", RoleOptions(name = "alex"))
        actor.click(originalTrigger).await()
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Create Player"))).await()
        act { dispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        (action as SavePlayerCommand).player.email.assertIsEqualTo("alex@example.invalid")
        screen.queryByRole("dialog").assertIsEqualTo(null)
        screen.getByRole("button", RoleOptions(name = "alex")).assertIsEqualTo(originalTrigger)
        document.activeElement.assertIsEqualTo(originalTrigger)
    }

    @Test
    fun saveCompletingAfterTabExitDoesNotPullFocusBack() = asyncSetup(object {
        val expectedParty = stubPartyDetails()
        val expectedContribution = stubContribution().copy(
            participantEmails = setOf("alex@example.invalid", "sam@example.invalid"),
            link = null,
        )
        val dispatcher = StubDispatcher.Channel()
        val actor = userEvent.setup()
    }) {
        render(RenderOptions(wrapper = MemoryRouter)) {
            UpdatingPlayerList(
                players = emptyList(),
                dispatchFunc = dispatcher.func<SavePlayerCommand.Dispatcher>(),
            ) { updatedPlayers, updatedDispatcher ->
                ContributionOverviewContent(
                    party = expectedParty,
                    contributions = listOf(expectedContribution),
                    players = updatedPlayers,
                    dispatchFunc = updatedDispatcher,
                )
            }
        }
        actor.click(screen.getByRole("button", RoleOptions(name = "alex"))).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Create Player"))).await()
        actor.tab().await()
        actor.tab().await()
    } exercise {
        act { dispatcher.onActionReturn(VoidResult.Accepted) }
    } verify {
        screen.queryByRole("dialog").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "sam")))
    }
}
