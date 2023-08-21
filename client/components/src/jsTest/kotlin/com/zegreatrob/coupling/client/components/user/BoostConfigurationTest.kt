package com.zegreatrob.coupling.client.components.user

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlinx.datetime.Instant
import kotlin.test.Test

class BoostConfigurationTest {

    @Test
    fun willShowCurrentBoostParty() = asyncSetup(object {
        val subscription = SubscriptionDetails(null, null, true, null)
        val parties = stubParties(4)
        val boostedParty = parties.random()
        val boost = Boost("user", setOf(boostedParty.id), Instant.DISTANT_FUTURE)
    }) exercise {
        render { BoostConfiguration(subscription = subscription, boost = boost, parties = parties) }
    } verify {
        screen.findAllByText(boostedParty.name!!)
            .assertIsNotEqualTo(null, "Should show name of currently boosted party")
    }

    private suspend fun partyCombobox() = screen.findByRole("combobox")

    @Test
    fun willAllowSelectionOfAnyParty() = asyncSetup(object {
        val subscription = SubscriptionDetails(null, null, true, null)
        val parties = stubParties(4)
        val boostedParty = parties.random()
        val actor = UserEvent.setup()
    }) {
        render { BoostConfiguration(subscription = subscription, boost = null, parties = parties) }
    } exercise {
        actor.selectOptions(partyCombobox(), boostedParty.id.value)
    } verify {
        within(partyCombobox())
            .getByRole("option", RoleOptions(selected = true))
            .getAttribute("value")
            .assertIsEqualTo(boostedParty.id.value)
    }
}
