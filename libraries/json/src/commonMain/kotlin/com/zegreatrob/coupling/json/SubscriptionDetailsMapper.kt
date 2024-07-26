package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.SubscriptionDetails

fun SubscriptionDetails.toJson() = GqlSubscriptionDetails(
    stripeCustomerId = stripeCustomerId,
    stripeSubscriptionId = stripeSubscriptionId,
    isActive = isActive,
    currentPeriodEnd = currentPeriodEnd,
)

fun GqlSubscriptionDetails.toModel() = SubscriptionDetails(
    stripeCustomerId = stripeCustomerId,
    stripeSubscriptionId = stripeSubscriptionId,
    isActive = isActive,
    currentPeriodEnd = currentPeriodEnd,
)
