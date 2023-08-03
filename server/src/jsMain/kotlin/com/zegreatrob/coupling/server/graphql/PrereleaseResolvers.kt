package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.boost.boostResolver
import com.zegreatrob.coupling.server.entity.boost.deleteBoostResolver
import com.zegreatrob.coupling.server.entity.boost.saveBoostResolver
import com.zegreatrob.coupling.server.express.Config
import kotlin.js.json

fun prereleaseResolvers() = json(
    "Mutation" to json(
        "saveBoost" to saveBoostResolver,
        "deleteBoost" to deleteBoostResolver,
    ),
    "User" to json(
        "boost" to boostResolver,
    ),
    "Configuration" to json(
        "stripePublishableKey" to { Config.stripePublishableKey },
        "addCreditCardSecret" to { Config.stripeSecretKey.let { "NO" } },
    ),
)
