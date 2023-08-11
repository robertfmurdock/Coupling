package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.SaveBoostCommand
import com.zegreatrob.coupling.action.SubscriptionCommandResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.repository.BoostSave
import com.zegreatrob.coupling.server.action.subscription.SubscriptionRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class ServerSaveBoostCommandTest {

    @Test
    fun whenNoSubscriptionIsActiveWillReturnThat() = asyncSetup(object : ServerSaveBoostCommandDispatcher {
        override val currentUser = stubUserDetails()
        val partyId = stubPartyId()
        val subscriptionSpyData = SpyData<String, SubscriptionDetails?>().apply { spyWillReturn(null) }
        override val subscriptionRepository = SubscriptionRepository(subscriptionSpyData::spyFunction)
        val boostSpyData = SpyData<Boost, Unit>().apply { spyWillReturn(Unit) }
        override val boostRepository = BoostSave(boostSpyData::spyFunction)
    }) exercise {
        perform(SaveBoostCommand(setOf(partyId)))
    } verify { result ->
        boostSpyData.spyReceivedValues.size
            .assertIsEqualTo(0)
        result.assertIsEqualTo(
            SubscriptionCommandResult.SubscriptionNotActive,
        )
    }

    @Test
    fun whenSubscriptionIsActiveCanSaveBoost() = asyncSetup(object : ServerSaveBoostCommandDispatcher {
        override val currentUser = stubUserDetails()
        val partyId = stubPartyId()
        val subscriptionDetails = SubscriptionDetails(
            stripeCustomerId = null,
            stripeSubscriptionId = null,
            isActive = true,
            currentPeriodEnd = null,
        )
        val subscriptionSpyData = SpyData<String, SubscriptionDetails?>().apply { spyWillReturn(subscriptionDetails) }
        override val subscriptionRepository = SubscriptionRepository(subscriptionSpyData::spyFunction)
        val boostSpyData = SpyData<Boost, Unit>().apply { spyWillReturn(Unit) }
        override val boostRepository = BoostSave(boostSpyData::spyFunction)
    }) exercise {
        perform(SaveBoostCommand(setOf(partyId)))
    } verify { result ->
        boostSpyData.spyReceivedValues
            .assertIsEqualTo(listOf(Boost(currentUser.id, setOf(partyId))))
        result.assertIsEqualTo(
            SaveBoostCommand.Result.Success,
        )
    }
}
