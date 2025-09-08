package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test

class DeleteSecretButtonTest {

    @Test
    fun onClickWillFireDeleteCommand() = asyncSetup(object {
        val partyId = stubPartyId()
        val secret = stubSecret()
        val dispatcher = StubDispatcher()
        val actor = UserEvent.Companion.setup()
    }) {
        TestingLibraryReact.render { DeleteSecretButton(partyId, secret, dispatcher.func(), {}) }
    } exercise {
        actor.click(TestingLibraryReact.screen.findByRole("button"))
    } verify {
        dispatcher.receivedActions
            .assertIsEqualTo(listOf(DeleteSecretCommand(partyId, secret.id)))
    }
}
