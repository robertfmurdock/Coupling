package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import org.w3c.dom.HTMLInputElement
import kotlin.test.Test

class CreateSecretPanelTest {
    @Test
    fun givenNoDescriptionClickWillNotFireCreateCommand() = asyncSetup(object {
        val partyId = stubPartyId()
        val dispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        js.globals.globalThis.alert = {}
        render { CreateSecretPanel(partyId, dispatcher.func()) }
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions("Create New Secret")))
    } verify {
        dispatcher.receivedActions
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun givenDescriptionClickWillFireCreateCommand() = asyncSetup(object {
        val partyId = stubPartyId()
        val dispatcher = StubDispatcher()
        val description = "We represent the lolly-pop kids!"
        val actor = UserEvent.setup()
    }) {
        render { CreateSecretPanel(partyId, dispatcher.func()) }
    } exercise {
        actor.type(screen.findByLabelText("Description"), description)
        actor.click(screen.findByRole("button", RoleOptions("Create New Secret")))
    } verify {
        dispatcher.receivedActions
            .assertIsEqualTo(listOf(CreateSecretCommand(partyId, description)))
    }

    @Test
    fun givenSuccessfulCommandWillShowSecretIdAndSecretValue() = asyncSetup(object {
        val partyId = stubPartyId()
        val dispatcher = StubDispatcher.Channel()
        val description = "We represent the lolly-pop kids!"
        val actor = UserEvent.setup()
        val expectedSecret = stubSecret()
        val expectedSecretValue = "Don't tell nobody!"
    }) {
        render { CreateSecretPanel(partyId, dispatcher.func()) }
        actor.type(screen.findByLabelText("Description"), description)
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions("Create New Secret")))
        act {
            dispatcher.onActionReturn(Pair(expectedSecret, expectedSecretValue))
        }
    } verify {
        val idInput = screen.getByLabelText("Secret ID") as HTMLInputElement
        val valueInput = screen.getByLabelText("Secret Value") as HTMLInputElement
        valueInput.value.assertIsEqualTo(expectedSecretValue)
        idInput.value.assertIsEqualTo(expectedSecret.id)
    }
}
