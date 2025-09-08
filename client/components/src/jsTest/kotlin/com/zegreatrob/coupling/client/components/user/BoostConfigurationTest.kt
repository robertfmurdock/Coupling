package com.zegreatrob.coupling.client.components.user

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test
import kotlin.time.Instant

class BoostConfigurationTest {

    @Test
    fun willShowCurrentBoostParty() = asyncSetup(object {
        val subscription = SubscriptionDetails(null, null, true, null)
        val parties = stubParties(4)
        val boostedParty = parties.random()
        val boost = Boost(UserId.Companion.new(), setOf(boostedParty.id), Instant.Companion.DISTANT_FUTURE)
    }) exercise {
        TestingLibraryReact.render {
            BoostConfiguration(
                subscription = subscription,
                boost = boost,
                parties = parties,
                dispatchFunc = StubDispatcher().func(),
                reload = {},
            )
        }
    } verify {
        TestingLibraryReact.screen.findAllByText(boostedParty.name!!)
            .assertIsNotEqualTo(null, "Should show name of currently boosted party")
    }

    private suspend fun partyCombobox() = TestingLibraryReact.screen.findByRole("combobox")

    @Test
    fun willAllowSelectionOfAnyParty() = asyncSetup(object {
        val subscription = SubscriptionDetails(null, null, true, null)
        val parties = stubParties(4)
        val boostedParty = parties.random()
        val actor = UserEvent.Companion.setup()
    }) {
        TestingLibraryReact.render {
            BoostConfiguration(
                subscription = subscription,
                boost = null,
                parties = parties,
                dispatchFunc = StubDispatcher().func(),
                reload = {},
            )
        }
    } exercise {
        actor.selectOptions(partyCombobox(), boostedParty.id.value.toString())
    } verify {
        TestingLibraryReact.within(partyCombobox())
            .getByRole("option", RoleOptions(selected = true))
            .getAttribute("value")
            .assertIsEqualTo(boostedParty.id.value.toString())
    }

    private val boostButton get() = TestingLibraryReact.screen.getByRole("button", RoleOptions(name = "Apply Boost"))

    @Test
    fun selectingPartyAndPressingButtonWillApplyBoost() = asyncSetup(object {
        val subscription = SubscriptionDetails(null, null, true, null)
        val parties = stubParties(4)
        val boostedParty = parties.random()
        val actor = UserEvent.Companion.setup()
        val dispatcher = StubDispatcher()
    }) {
        TestingLibraryReact.render {
            BoostConfiguration(
                subscription = subscription,
                boost = null,
                parties = parties,
                dispatchFunc = dispatcher.func(),
                reload = {},
            )
        }
        actor.selectOptions(partyCombobox(), boostedParty.id.value.toString())
    } exercise {
        actor.click(boostButton)
    } verify {
        dispatcher.receivedActions
            .assertIsEqualTo(listOf(ApplyBoostCommand(boostedParty.id)))
    }
}
