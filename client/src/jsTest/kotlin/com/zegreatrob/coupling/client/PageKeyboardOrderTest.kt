package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.party.PartyList
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlin.test.AfterTest
import kotlin.test.Test

class PageKeyboardOrderTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun partyListHeaderPlacesInformationAfterItsNavigation() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderNavigation { PartyList { parties = emptyList() } }
    } exercise {
        (1..5).map {
            actor.tab().await()
            document.activeElement?.let { it.getAttribute("aria-label") ?: it.textContent?.trim() }
        }
    } verify { names ->
        names.assertIsEqualTo(listOf("About", "Demo", "Sign Out", "GraphiQL", "Recent information"))
    }

    @Test
    fun partyHeaderPlacesPartyNavigationBeforeUtilitiesAndInformation() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderNavigation {
            ConfigHeader {
                party = PartyDetails(PartyId("keyboard-party"))
                +"A party"
            }
        }
        screen.getByRole("link", RoleOptions(name = "Party Home Page")).focus()
    } exercise {
        (1..10).map {
            actor.tab().await()
            document.activeElement?.let { it.getAttribute("aria-label") ?: it.textContent?.trim() }
        }
    } verify { names ->
        names.assertIsEqualTo(
            listOf(
                "Settings", "Add Player!", "History!", "Pin Bag!", "Statistics!", "Retirees!",
                "Party select", "Sign Out", "GraphiQL", "Recent information",
            ),
        )
    }

    @Test
    fun pairingPagePlacesSpinImmediatelyBeforeCopyImage() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderNavigation {
            PairAssignments {
                party = PartyDetails(PartyId("keyboard-party"))
                players = emptyList()
                pairs = null
                setPairs = {}
                controls = Controls({ {} }) {}
                message = CouplingSocketMessage("", emptySet())
                allowSave = false
            }
        }
        screen.getByRole("link", RoleOptions(name = "Prepare to spin!")).focus()
    } exercise {
        actor.tab().await()
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Copy pair image to clipboard")))
    }
}
