package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CloseButton
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.coroutines.await
import react.create
import react.dom.html.ReactHTML.form
import kotlin.test.AfterTest
import kotlin.test.Test

class CloseControlTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun closingInvokesDismissalWithoutSubmittingEnclosingForm() = asyncSetup(object {
        val actor = userEvent.setup()
        var dismissals = 0
        var submissions = 0
    }) {
        render(
            form.create {
                onSubmit = {
                    it.preventDefault()
                    submissions += 1
                }
                CloseButton { onClose = { dismissals += 1 } }
            },
        )
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close"))).await()
    } verify {
        dismissals.assertIsEqualTo(1)
        submissions.assertIsEqualTo(0)
    }
}
