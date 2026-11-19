package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import react.create
import kotlin.test.Test

class GeneralControlBarTest {
    @Test
    fun pageHeadingNamesOnlyTheTitleAndLeavesNavigationSeparate() = setup() exercise {
        render(
            GeneralControlBar.create {
                title = "Party List"
                showNotifications = true
                CouplingLink {
                    to = "/about"
                    +"About"
                }
            },
            RenderOptions(wrapper = TestRouter),
        )
    } verify {
        val heading = screen.getByRole("heading", RoleOptions(name = "Party List"))
        heading.tagName.assertIsEqualTo("H1")
        heading.querySelector("a, button, input, select, textarea, [tabindex]").assertIsEqualTo(null)
        screen.getByRole("link", RoleOptions(name = "About")).getAttribute("href").assertIsEqualTo("/about")
        screen.getByRole("button", RoleOptions(name = "Recent information")).tagName.assertIsEqualTo("BUTTON")
    }
}
