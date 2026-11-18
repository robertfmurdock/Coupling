package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlin.test.Test

class PlayerCardNamingTest {
    @Test
    fun rosterLinkUsesTheVisiblePlayerNameWithoutGenericImageText() = setup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer().copy(name = "Alex")
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) {
            PlayerRoster {
                partyId = expectedParty.id
                players = listOf(expectedPlayer)
            }
        }
    } verify {
        screen.getByRole("link", RoleOptions(name = "Alex")).textContent.assertIsEqualTo("Alex")
    }

    @Test
    fun compactRosterLinkUsesTheVisiblePlayerNameWithoutGenericImageText() = setup(object {
        val expectedParty = stubPartyDetails()
        val expectedPlayer = stubPlayer().copy(name = "Alex")
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) { TinyPlayerList(expectedParty, listOf(expectedPlayer)) }
    } verify {
        screen.getByRole("link", RoleOptions(name = "Alex")).textContent.assertIsEqualTo("Alex")
    }
}
