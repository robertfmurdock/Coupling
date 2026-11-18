package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.player.PlayerConfigContent
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlin.test.AfterTest
import kotlin.test.Test

class PlayerConfigNamingTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun emailDescribesThePartyAccessItGrants() = asyncSetup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer()
    }) exercise {
        renderNavigation {
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
    fun badgeOptionsUseThePartyConfiguredVisibleNames() = asyncSetup(object {
        val expectedParty = stubPartyDetails().copy(
            badgesEnabled = true,
            defaultBadgeName = "Navigator",
            alternateBadgeName = "Driver",
        )
        val expectedPlayer = stubPlayer()
    }) exercise {
        renderNavigation {
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
