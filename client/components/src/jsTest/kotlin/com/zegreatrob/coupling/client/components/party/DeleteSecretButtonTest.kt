package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test

class DeleteSecretButtonTest {

    @Test
    fun onClickWillFireDeleteCommand() = asyncSetup(object {
        val partyId = stubPartyId()
        val secret = stubSecret()
        val dispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render { DeleteSecretButton(partyId, secret, dispatcher.func()) }
    } exercise {
        actor.click(screen.findByRole("button"))
    } verify {
        dispatcher.receivedActions
            .assertIsEqualTo(listOf(DeleteSecretCommand(partyId, secret.id)))
    }
}
