package com.zegreatrob.coupling.client.components

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlin.test.Test

class NavigationLinkTest {

    @Test
    fun graphiqlNavigationHasOneNamedInteractiveTarget() = setup(Unit) exercise {
        render(RenderOptions(wrapper = TestRouter)) { GqlButton() }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "GraphiQL"))
        link.getAttribute("href").assertIsEqualTo("/graphiql")
        link.getAttribute("tabindex").assertIsEqualTo(null)
        link.querySelector("button, a, input, select, textarea, [tabindex]").assertIsEqualTo(null)
    }

    @Test
    fun demoNavigationHasOneNamedInteractiveTarget() = setup(Unit) exercise {
        render(RenderOptions(wrapper = TestRouter)) { DemoButton() }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "Demo"))
        link.getAttribute("href").assertIsEqualTo("/demo")
        link.getAttribute("tabindex").assertIsEqualTo(null)
        link.querySelector("button, a, input, select, textarea, [tabindex]").assertIsEqualTo(null)
    }
}
