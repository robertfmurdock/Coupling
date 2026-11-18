package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.demo.CurrentPairs
import com.zegreatrob.coupling.client.demo.DemoAnimationState
import com.zegreatrob.coupling.client.demo.DemoPageFrame
import com.zegreatrob.coupling.client.demo.MakeParty
import com.zegreatrob.coupling.client.demo.Start
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import react.dom.html.ReactHTML.button
import kotlin.test.AfterTest
import kotlin.test.Test

class DemoInteractionTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun introductionKeepsItsRealReturnLinkFocusable() = asyncSetup {
        renderNavigation { DemoPageFrame(Start) }
    } exercise {
        screen.getByRole("link", RoleOptions(name = "Back to Coupling!")).focus()
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("link", RoleOptions(name = "Back to Coupling!")))
    }

    @Test
    fun finalFrameKeepsRealReturnLinkFocusableOutsideTheIllustration() = asyncSetup {
        val finalState = DemoAnimationState.generateSequence().last().data as CurrentPairs
        renderNavigation { DemoPageFrame(finalState) }
        waitFor { screen.getByRole("link", RoleOptions(name = "Back to Coupling!")) }.await()
    } exercise {
        screen.getByRole("link", RoleOptions(name = "Back to Coupling!")).focus()
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("link", RoleOptions(name = "Back to Coupling!")))
    }

    @Test
    fun illustratedPartyFormCannotTakeFocus() = asyncSetup {
        renderNavigation {
            button { +"Outside the illustration" }
            DemoPageFrame(MakeParty(stubPartyDetails()))
        }
        screen.getByRole("button", RoleOptions(name = "Outside the illustration")).focus()
    } exercise {
        (document.querySelector("input[name=name]") as HTMLElement).focus()
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Outside the illustration")))
    }
}
