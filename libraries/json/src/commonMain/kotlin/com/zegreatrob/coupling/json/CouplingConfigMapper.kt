package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingConfig

fun GqlConfiguration.toModel() = CouplingConfig(
    discordClientId = discordClientId,
    addToSlackUrl = addToSlackUrl,
    stripeAdminCode = stripeAdminCode,
    stripePurchaseCode = stripePurchaseCode,
)
