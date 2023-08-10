package com.zegreatrob.coupling.model

data class CouplingConfig(
    val discordClientId: String? = null,
    val addToSlackUrl: String? = null,
    val stripeAdminCode: String? = null,
    val stripePurchaseCode: String? = null,
)
