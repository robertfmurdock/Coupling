package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.server.action.subscription.SubscriptionRepository
import com.zegreatrob.coupling.server.external.stripe.StripeCustomer
import js.core.jso
import kotlinx.coroutines.await

class StripeSubscriptionRepository : SubscriptionRepository {
    override suspend fun findSubscriptionDetails(email: String): SubscriptionDetails? {
        val customers = stripe.customers.list(jso { this.email = email }).await()
        return customers.data.firstOrNull()?.let {
            SubscriptionDetails(
                stripeCustomerId = it.id,
                stripeSubscriptionId = it.findSubscription()?.id,
            )
        }
    }

    private suspend fun StripeCustomer.findSubscription() = stripe.subscriptions.list(jso { this.customer = id })
        .await()
        .data
        .firstOrNull()
}
