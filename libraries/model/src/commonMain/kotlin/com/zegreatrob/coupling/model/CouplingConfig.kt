package com.zegreatrob.coupling.model

data class CouplingConfig(
    val discordClientId: String? = null,
    val addToSlackUrl: String? = null,
    val addCreditCardSecret: String? = null,
    val stripePublishableKey: String? = null,
)
