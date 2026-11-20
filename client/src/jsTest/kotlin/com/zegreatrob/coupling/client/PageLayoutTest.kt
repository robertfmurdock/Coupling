package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.contribution.ContributionCardHeader
import com.zegreatrob.coupling.client.components.contribution.ContributionQuickStatsPanel
import com.zegreatrob.coupling.client.components.external.reactdnd.dndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5Backend
import com.zegreatrob.coupling.client.components.pairassignments.CurrentPairAssignmentsPanel
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.party.PartyList
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import emotion.react.css
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotools.types.collection.notEmptyListOf
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.integer
import web.cssom.px
import web.dom.ElementId
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.Test

class PageLayoutTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun contributionSummaryStaysVisibleWhenSurroundingPageOverflows() = asyncSetup() exercise {
        renderNavigation {
            div {
                id = ElementId("overflowing-summary-host")
                css {
                    width = (window.innerWidth * 2).px
                    textAlign = TextAlign.center
                }
                ContributionQuickStatsPanel(5, kotlin.time.Duration.parse("20m"), 3, 2)
            }
        }
    } verify {
        val host = document.getElementById("overflowing-summary-host")!!
        val summary = host.querySelector("section")!!.getBoundingClientRect()
        (summary.left >= 0 && summary.right <= window.innerWidth).assertIsEqualTo(true)
    }

    @Test
    fun contributionSummaryFitsNarrowContainerWithEnlargedText() = asyncSetup() exercise {
        renderNavigation {
            div {
                id = ElementId("summary-layout-host")
                css {
                    width = 320.px
                    fontSize = 32.px
                }
                ContributionQuickStatsPanel(5, kotlin.time.Duration.parse("1d 12h 30m"), 3, 4)
            }
        }
    } verify {
        val host = document.getElementById("summary-layout-host")!!
        val panel = host.querySelector("section")!!
        val bounds = host.getBoundingClientRect()
        (panel.getBoundingClientRect().width <= bounds.width).assertIsEqualTo(true)
        panel.querySelectorAll("dt, dd").let { metrics ->
            (0 until metrics.length).forEach { index ->
                val metric = (metrics.item(index) as org.w3c.dom.Element).getBoundingClientRect()
                (metric.left >= bounds.left && metric.right <= bounds.right).assertIsEqualTo(true)
            }
        }
    }

    @Test
    fun pairingActionRemainsClickableAboveNeighboringControls() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderEditablePairs()
        actor.click(screen.getByRole("button", RoleOptions(name = "Alex"))).await()
    } exercise {
        val action = screen.getByRole("button", RoleOptions(name = "Swap with…"))
        val bounds = action.getBoundingClientRect()
        val neighbor = document.createElement("button") as org.w3c.dom.HTMLElement
        neighbor.textContent = "Spin"
        neighbor.style.cssText = "position:fixed;z-index:20;left:${bounds.left}px;top:${bounds.top}px;width:${bounds.width}px;height:${bounds.height}px"
        document.body!!.appendChild(neighbor)
        try {
            document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2)
        } finally {
            neighbor.remove()
        }
    } verify { hit ->
        hit.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Swap with…")))
    }

    @Test
    fun playerDragUsesTheVisibleCardAndKeepsTheGrabPointInsideIt() = asyncSetup {
        renderEditablePairs()
    } exercise {
        val card = screen.getByRole("button", RoleOptions(name = "Alex")).parentElement!!
        capturesDragImageWithinCard(card)
    } verify { aligned ->
        aligned.assertIsEqualTo(true)
    }

    private fun renderEditablePairs() = renderNavigation {
        val alex = stubPlayer().copy(name = "Alex")
        val sam = stubPlayer().copy(name = "Sam")
        dndProvider {
            backend = html5Backend
            div {
                css {
                    position = Position.relative
                    zIndex = integer(1)
                }
                CurrentPairAssignmentsPanel(
                    party = PartyDetails(PartyId("editable-layout-party")),
                    pairAssignments = stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(alex).withPins(), pairOf(sam).withPins())),
                    setPairAssignments = {},
                    allowSave = true,
                    dispatchFunc = { {} },
                )
            }
        }
    }

    @Test
    fun contributorActionPreservesCardGeometryAndVisibleFocus() = asyncSetup(object {
        val alex = stubPlayer().copy(name = "Alex")
        val sam = stubPlayer().copy(name = "Sam")
        val expectedContribution = stubContribution().copy(participantEmails = setOf(alex.email, sam.email), link = null)
        val actor = userEvent.setup()
    }) {
        renderNavigation {
            listOf(false, true).forEach { interactive ->
                div {
                    id = ElementId(if (interactive) "action-header-host" else "static-header-host")
                    css {
                        width = 800.px
                        height = 100.px
                    }
                    ContributionCardHeader {
                        partyId = PartyId("contributor-layout")
                        contribution = expectedContribution
                        players = listOf(alex, sam)
                        if (interactive) onPlayerClick = { _, _ -> }
                    }
                }
            }
        }
    } exercise {
        actor.tab().await()
    } verify {
        val staticHost = document.getElementById("static-header-host")!!
        val actionHost = document.getElementById("action-header-host")!!
        val before = staticHost.querySelector("[data-player-id]")!!.getBoundingClientRect()
        val after = actionHost.querySelector("[data-player-id]")!!.getBoundingClientRect()
        after.width.assertIsEqualTo(before.width)
        after.height.assertIsEqualTo(before.height)
        (
            abs(
                (after.left - actionHost.getBoundingClientRect().left) -
                    (before.left - staticHost.getBoundingClientRect().left),
            ) < 0.5
            ).assertIsEqualTo(true)
        (
            abs(
                (after.top - actionHost.getBoundingClientRect().top) -
                    (before.top - staticHost.getBoundingClientRect().top),
            ) < 0.5
            ).assertIsEqualTo(true)
        val trigger = screen.getByRole("button", RoleOptions(name = "Alex"))
        document.activeElement.assertIsEqualTo(trigger)
        val style = window.getComputedStyle(trigger)
        (style.outlineStyle != "none" && style.outlineWidth != "0px").assertIsEqualTo(true)
    }

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
    fun narrowAboutKeepsArticleAndNavigationWithinItsContainer() = asyncSetup() exercise {
        renderNavigation {
            div {
                id = ElementId("about-test-viewport")
                css { width = 320.px }
                aboutPageContent {
                    p { +"A readable article with enough words to wrap across several lines in a narrow window." }
                }
            }
        }
    } verify {
        val host = document.getElementById("about-test-viewport")!!
        (host.scrollWidth <= host.clientWidth).assertIsEqualTo(
            true,
            "About fits without horizontal scrolling: ${host.scrollWidth} within ${host.clientWidth}",
        )
        val bounds = host.getBoundingClientRect()
        val back = screen.getByRole("link", RoleOptions(name = "Back to Coupling!")).getBoundingClientRect()
        (back.left >= bounds.left && back.right <= bounds.right).assertIsEqualTo(true)
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

private val capturesDragImageWithinCard: (org.w3c.dom.Element) -> Boolean = js(
    """(card => {
        const box = card.getBoundingClientRect();
        const clientX = box.left + box.width / 2;
        const clientY = box.top + box.height / 2;
        const target = document.elementFromPoint(clientX, clientY);
        const transfer = new DataTransfer();
        let aligned = false;
        transfer.setDragImage = (image, x, y) => {
            aligned = image === card && x >= 0 && x <= box.width && y >= 0 && y <= box.height;
        };
        target.dispatchEvent(new DragEvent('dragstart', {bubbles: true, cancelable: true, dataTransfer: transfer, clientX, clientY}));
        target.dispatchEvent(new DragEvent('dragend', {bubbles: true, dataTransfer: transfer}));
        return aligned;
    })""",
)
