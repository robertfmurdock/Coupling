package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.party.PartyList
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.px
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.Test

class PageLayoutTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun aboutHeaderCentersTitleAndBackAsOneGroup() = asyncSetup() exercise {
        renderNavigation {
            aboutPageContent { +"Article content" }
        }
    } verify {
        val back = screen.getByRole("link", RoleOptions(name = "Back to Coupling!")).getBoundingClientRect()
        val title = screen.getByRole("heading", RoleOptions(name = "About This App"))
        val heading = title.getBoundingClientRect()
        val header = title.parentElement!!.getBoundingClientRect()
        (heading.right <= back.left).assertIsEqualTo(true)
        (heading.left > header.left + 16).assertIsEqualTo(true, "Title is inset from the content edge")
        (abs((heading.left - header.left) - (header.right - back.right)) <= 4)
            .assertIsEqualTo(true, "Group is centered allowing the link's existing margins")
        (back.top < heading.bottom && back.bottom > heading.top).assertIsEqualTo(true)
    }

    @Test
    fun widePartyListHeaderKeepsTitleBesideNavigation() = asyncSetup() exercise {
        renderNavigation {
            div {
                css { width = 1200.px }
                PartyList { parties = emptyList() }
            }
        }
    } verify {
        val title = screen.getByText("Party List").getBoundingClientRect()
        val about = screen.getByRole("link", RoleOptions(name = "About")).getBoundingClientRect()
        (title.right <= about.left).assertIsEqualTo(true)
        (title.top < about.bottom && title.bottom > about.top).assertIsEqualTo(true)
    }

    @Test
    fun copyImageStaysInTheRightHandRailAboveSpin() = asyncSetup() exercise {
        renderNavigation {
            PairAssignments {
                party = PartyDetails(PartyId("layout-party"))
                players = emptyList()
                pairs = null
                setPairs = {}
                controls = Controls({ {} }) {}
                message = CouplingSocketMessage("", emptySet())
                allowSave = false
            }
        }
    } verify {
        val copy = screen.getByRole("button", RoleOptions(name = "Copy pair image to clipboard")).getBoundingClientRect()
        val spin = screen.getByRole("link", RoleOptions(name = "Prepare to spin!")).getBoundingClientRect()
        (copy.bottom <= spin.top).assertIsEqualTo(true)
        (copy.left >= spin.right).assertIsEqualTo(true)
    }
}
