package com.zegreatrob.coupling.client.components.slack

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import react.ReactNode
import react.create
import react.router.MemoryRouter
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.Test

class SlackConnectPageContentTest {

    private val saveButton get() = screen.getByRole("button", RoleOptions(name = "Save"))
    private val returnButton get() = screen.queryByRole("button", RoleOptions(name = "Return to Coupling"))
    private val partySelect get() = screen.getByLabelText("Party")

    @Test
    fun willSendSaveCommandOnSave() = asyncSetup(object {
        val stubber = StubDispatcher.Channel()
        val actor = UserEvent.setup()
        val parties = stubParties(6)
        val targetParty = parties.random()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) {
        render(jso { wrapper = MemoryRouter }) {
            SlackConnectPageContent(
                parties = parties,
                slackTeam = slackTeam,
                slackChannel = slackChannel,
                dispatchFunc = stubber.func(),
            )
        }
        actor.selectOptions(partySelect, targetParty.id.value)
        returnButton
            .assertIsEqualTo(null, "Return button showed up unexpectedly early")
    } exercise {
        actor.click(saveButton)
        act { stubber.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(
            SaveSlackIntegrationCommand(
                partyId = targetParty.id,
                team = slackTeam,
                channel = slackChannel,
            ),
        )
        returnButton.assertIsNotEqualTo(null)
    }

    @Test
    fun willNotShowReturnImmediately() = asyncSetup(object {
    }) exercise {
        render {
            SlackConnectPageContent(
                parties = stubParties(2),
                slackTeam = uuidString(),
                slackChannel = uuidString(),
                dispatchFunc = { {} },
            )
        }
    } verify {
        returnButton
            .assertIsEqualTo(null, "Return button showed up unexpectedly early")
    }

    @Test
    fun afterSaveReturnButtonTakesYouToParty() = asyncSetup(object : ScopeMint() {
        val actor = UserEvent.setup()
        val party = stubPartyDetails()
        val stubDispatcher = StubDispatcher.Channel()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("Party Time")
                        },
                        jso {
                            path = "*"
                            element = SlackConnectPageContent.create(
                                parties = listOf(party),
                                slackTeam = uuidString(),
                                slackChannel = uuidString(),
                                dispatchFunc = stubDispatcher.func(),
                            )
                        },
                    ),
                )
            },
        )
        actor.click(saveButton)
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } exercise {
        actor.click(returnButton)
    } verify {
        screen.queryByText("Party Time")
            .assertIsNotEqualTo(null, "Didn't end up at party page")
    }
}
