package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.reactdnd.dndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5Backend
import com.zegreatrob.coupling.client.components.pairassignments.spin.PairAssignmentsAnimator
import com.zegreatrob.coupling.client.components.welcome.Welcome
import com.zegreatrob.coupling.client.demo.returnToCouplingButton
import com.zegreatrob.coupling.client.routing.animationFrame
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPinnedCouplingPair
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.react.dataloader.ResolvedState
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.events.Event
import react.dom.html.ReactHTML.button
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Clock

class ReducedMotionTest {
    private val originalMatchMedia = window.asDynamic().matchMedia
    private val media = js("new EventTarget()")

    private fun preference(reduced: Boolean) {
        media.matches = reduced
        window.asDynamic().matchMedia = { _: String -> media }
    }

    @AfterTest
    fun cleanUp() {
        cleanup()
        window.asDynamic().matchMedia = originalMatchMedia
    }

    @Test
    fun reducedMotionKeepsResolvedRouteContentVisibleWithoutEntranceAnimation() = asyncSetup {
        preference(true)
    } exercise {
        renderNavigation {
            animationFrame {
                state = ResolvedState(Unit)
                button { +"Route action" }
            }
        }
    } verify {
        val action = screen.getByRole("button", RoleOptions(name = "Route action"))
        window.getComputedStyle(action.parentElement!!).animationName.assertIsEqualTo("none")
    }

    @Test
    fun reducedMotionRemovesWelcomeEntranceAndPulseAnimations() = asyncSetup {
        preference(true)
    } exercise {
        renderNavigation { Welcome() }
    } verify {
        val elements = document.querySelectorAll("body *")
        (0 until elements.length).mapNotNull { elements.item(it) as? org.w3c.dom.Element }
            .filter { window.getComputedStyle(it).animationName.let { name -> name != "none" && name.isNotEmpty() } }
            .size.assertIsEqualTo(0)
        screen.getByRole("button", RoleOptions(name = "Come on in!"))
    }

    @Test
    fun reducedMotionRemovesNotificationAndDemoPulses() = asyncSetup {
        preference(true)
    } exercise {
        renderNavigation {
            NotificationButton()
            returnToCouplingButton()
        }
    } verify {
        window.getComputedStyle(screen.getByRole("button", RoleOptions(name = "Recent information")))
            .animationName.assertIsEqualTo("none")
        window.getComputedStyle(screen.getByRole("link", RoleOptions(name = "Back to Coupling!")))
            .animationName.assertIsEqualTo("none")
    }

    @Test
    fun reducedMotionImmediatelyShowsFinalPairs() = asyncSetup {
        preference(true)
    } exercise {
        renderPairs()
    } verify {
        screen.getByRole("button", RoleOptions(name = "Final pairs action")).textContent
            .assertIsEqualTo("Final pairs action")
    }

    @Test
    fun enablingReducedMotionDuringSpinShowsFinalPairs() = asyncSetup {
        preference(false)
        renderPairs()
    } exercise {
        media.matches = true
        media.dispatchEvent(Event("change"))
    } verify {
        waitFor {
            screen.getByRole("button", RoleOptions(name = "Final pairs action")).textContent
                .assertIsEqualTo("Final pairs action")
        }.await()
    }

    @Test
    fun explicitAnimationDisableStillShowsFinalPairs() = asyncSetup {
        preference(false)
    } exercise {
        renderPairs(disabled = true)
    } verify {
        screen.getByRole("button", RoleOptions(name = "Final pairs action")).textContent
            .assertIsEqualTo("Final pairs action")
    }

    private fun renderPairs(disabled: Boolean = false) = renderNavigation {
        animationsDisabledContext(disabled) {
            dndProvider {
                backend = html5Backend
                PairAssignmentsAnimator {
                    party = stubPartyDetails()
                    players = emptyList()
                    pairAssignments = PairingSet(PairingSetId.new(), Clock.System.now(), notEmptyListOf(stubPinnedCouplingPair()))
                    enabled = true
                    button { +"Final pairs action" }
                }
            }
        }
    }
}
