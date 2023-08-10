package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.boost.deleteBoostResolver
import com.zegreatrob.coupling.server.entity.boost.partyBoostResolver
import com.zegreatrob.coupling.server.entity.boost.saveBoostResolver
import com.zegreatrob.coupling.server.entity.boost.userBoostResolver
import com.zegreatrob.coupling.server.entity.subscription.subscriptionResolver
import kotlin.js.json

fun prereleaseResolvers() = json(
    "Mutation" to json(
        "saveBoost" to saveBoostResolver,
        "deleteBoost" to deleteBoostResolver,
    ),
    "User" to json(
        "boost" to userBoostResolver,
        "subscription" to subscriptionResolver,
    ),
    "Party" to json(
        "boost" to partyBoostResolver,
    ),
)
