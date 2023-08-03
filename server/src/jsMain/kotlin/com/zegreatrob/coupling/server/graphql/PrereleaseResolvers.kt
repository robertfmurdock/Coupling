package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.boost.boostResolver
import com.zegreatrob.coupling.server.entity.boost.deleteBoostResolver
import com.zegreatrob.coupling.server.entity.boost.saveBoostResolver
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.stripe.stripe
import js.core.jso
import kotlin.js.json

val stripe by lazy { stripe(Config.stripeSecretKey) }

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
        "addCreditCardSecret" to {
            stripe.setupIntents.create(jso { paymentMethodTypes = arrayOf("card") }).then { it.clientSecret }
        },
    ),
)
