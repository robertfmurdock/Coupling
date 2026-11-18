package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.DescribedRoleOptions
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlin.test.Test
import com.zegreatrob.coupling.client.components.accessibilityScreen as screen

class PlayerConfigAccessibilityTest {
    @Test
    fun emailDescribesThePartyAccessItGrants() = setup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer()
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) {
            PlayerConfigContent {
                party = expectedParty
                player = expectedPlayer
                players = emptyList()
                onChange = {}
                onSubmit = {}
                onPlayerChange = {}
            }
        }
    } verify {
        screen.getByRole(
            "textbox",
            DescribedRoleOptions(
                name = "Email",
                description = "Email provides access privileges; " +
                    "That means if someone with that email logs into Coupling, they can see their party!",
            ),
        ).getAttribute("value").assertIsEqualTo(expectedPlayer.email)
    }

    @Test
    fun badgeOptionsUseThePartyConfiguredVisibleNames() = setup(object {
        val expectedParty = stubPartyDetails().copy(
            badgesEnabled = true,
            defaultBadgeName = "Navigator",
            alternateBadgeName = "Driver",
        )
        val expectedPlayer = stubPlayer()
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) {
            PlayerConfigContent {
                party = expectedParty
                player = expectedPlayer
                players = emptyList()
                onChange = {}
                onSubmit = {}
                onPlayerChange = {}
            }
        }
    } verify {
        screen.getByRole("combobox", RoleOptions(name = "Badge")).getAttribute("name")
            .assertIsEqualTo("badge")
        screen.getByRole("option", RoleOptions(name = expectedParty.defaultBadgeName)).getAttribute("value")
            .assertIsEqualTo("Default")
        screen.getByRole("option", RoleOptions(name = expectedParty.alternateBadgeName)).getAttribute("value")
            .assertIsEqualTo("Alternate")
    }
}
