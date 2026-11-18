package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.player.PlayerRoster
import com.zegreatrob.coupling.client.components.player.TinyPlayerList
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlin.test.AfterTest
import kotlin.test.Test

class PlayerCardNamingTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun rosterLinkUsesTheVisiblePlayerNameWithoutGenericImageText() = asyncSetup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer().copy(name = "Alex")
    }) exercise {
        renderNavigation {
            PlayerRoster {
                partyId = expectedParty.id
                players = listOf(expectedPlayer)
            }
        }
    } verify {
        screen.getByRole("link", RoleOptions(name = "Alex")).textContent.assertIsEqualTo("Alex")
    }

    @Test
    fun compactRosterLinkUsesTheVisiblePlayerNameWithoutGenericImageText() = asyncSetup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer().copy(name = "Alex")
    }) exercise {
        renderNavigation { TinyPlayerList(expectedParty, listOf(expectedPlayer)) }
    } verify {
        screen.getByRole("link", RoleOptions(name = "Alex")).textContent.assertIsEqualTo("Alex")
    }
}
