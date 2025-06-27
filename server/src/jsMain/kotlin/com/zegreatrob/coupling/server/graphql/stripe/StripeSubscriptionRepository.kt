package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.server.action.subscription.SubscriptionRepository
import com.zegreatrob.coupling.server.external.stripe.StripeCustomer
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotlin.time.Instant

class StripeSubscriptionRepository : SubscriptionRepository {
    override suspend fun findSubscriptionDetails(email: String): SubscriptionDetails? {
        val customers = stripe.customers.list(unsafeJso { this.email = email }).await()
        val customer = customers.data.firstOrNull()
        val subscription = customer?.findSubscription()
        return customer?.let {
            SubscriptionDetails(
                stripeCustomerId = it.id,
                stripeSubscriptionId = subscription?.id,
                isActive = subscription?.status == "active",
                currentPeriodEnd = Instant.fromEpochSeconds(subscription?.currentPeriodEnd?.toLong() ?: 0),
            )
        }
    }

    private suspend fun StripeCustomer.findSubscription() = stripe.subscriptions.list(unsafeJso { this.customer = id })
        .await()
        .data
        .firstOrNull()
}
