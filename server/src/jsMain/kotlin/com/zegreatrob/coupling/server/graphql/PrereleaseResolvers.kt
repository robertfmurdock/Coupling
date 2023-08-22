package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.boost.deleteBoostResolver
import com.zegreatrob.coupling.server.entity.boost.saveBoostResolver
import com.zegreatrob.coupling.server.entity.contribution.saveContributionResolver
import com.zegreatrob.coupling.server.entity.subscription.subscriptionResolver
import kotlin.js.json

fun prereleaseResolvers() = json(
    "Mutation" to json(
        "saveBoost" to saveBoostResolver,
        "deleteBoost" to deleteBoostResolver,
        "saveContribution" to saveContributionResolver,
    ),
    "User" to json(
        "subscription" to subscriptionResolver,
    ),
    "Configuration" to json(
        "stripeAdminCode" to { "test_4gw9BcbgqaeYaRybII" },
        "stripePurchaseCode" to { "test_fZe5kta5OcHOfkI7ss" },
    ),
)
