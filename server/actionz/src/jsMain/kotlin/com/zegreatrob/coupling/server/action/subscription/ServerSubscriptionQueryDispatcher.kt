package com.zegreatrob.coupling.server.action.subscription

import com.zegreatrob.coupling.action.subscription.SubscriptionQuery
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.SubscriptionDetails

interface ServerSubscriptionQueryDispatcher :
    SubscriptionQuery.Dispatcher,
    CurrentUserProvider {

    val subscriptionRepository: SubscriptionRepository

    override suspend fun perform(query: SubscriptionQuery): SubscriptionDetails? = subscriptionRepository.findSubscriptionDetails(currentUser.email.toString())
}

fun interface SubscriptionRepository {
    suspend fun findSubscriptionDetails(email: String): SubscriptionDetails?
}
