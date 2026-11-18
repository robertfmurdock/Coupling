package com.zegreatrob.coupling.client.components

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.dom.html.ReactHTML.form
import kotlin.test.Test

class CloseControlTest {
    @Test
    fun closingInvokesDismissalWithoutSubmittingEnclosingForm() = asyncSetup(object {
        val actor = UserEvent.setup()
        var dismissals = 0
        var submissions = 0
    }) {
        render {
            form {
                onSubmit = {
                    it.preventDefault()
                    submissions += 1
                }
                CloseButton { onClose = { dismissals += 1 } }
            }
        }
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close")))
    } verify {
        dismissals.assertIsEqualTo(1)
        submissions.assertIsEqualTo(0)
    }
}
