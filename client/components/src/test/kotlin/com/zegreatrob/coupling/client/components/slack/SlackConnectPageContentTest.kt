package com.zegreatrob.coupling.client.components.slack

import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test

class SlackConnectPageContentTest {

    private val saveButton get() = TestingLibraryReact.screen.getByRole("button", RoleOptions(name = "Save"))
    private val partySelect get() = TestingLibraryReact.screen.getByLabelText("Party")

    @Test
    fun willSendSaveCommandOnSave() = asyncSetup(object {
        val stubber = StubDispatcher()
        val actor = UserEvent.setup()
        val parties = stubParties(6)
        val targetParty = parties.random()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) {
        render(
            SlackConnectPageContent(
                parties = parties,
                slackTeam = slackTeam,
                slackChannel = slackChannel,
                dispatchFunc = stubber.func(),
            ).create {},
        )
        actor.selectOptions(partySelect, targetParty.id.value)
    } exercise {
        actor.click(saveButton)
    } verify {
        stubber.commandsDispatched<SaveSlackIntegrationCommand>()
            .assertIsEqualTo(
                listOf(
                    SaveSlackIntegrationCommand(
                        partyId = targetParty.id,
                        team = slackTeam,
                        channel = slackChannel,
                    ),
                ),
            )
    }
}
