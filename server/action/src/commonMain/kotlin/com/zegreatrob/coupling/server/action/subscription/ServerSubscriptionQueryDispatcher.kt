package com.zegreatrob.coupling.server.action.subscription

import com.zegreatrob.coupling.action.subscription.SubscriptionQuery
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.SubscriptionDetails

interface ServerSubscriptionQueryDispatcher : SubscriptionQuery.Dispatcher, CurrentUserProvider {

    val stripeRepository: StripeRepository

    override suspend fun perform(query: SubscriptionQuery): SubscriptionDetails? {
        return stripeRepository.findSubscriptionDetails(currentUser.email)
    }
}

interface StripeRepository {
    fun findSubscriptionDetails(email: String): SubscriptionDetails?
}
