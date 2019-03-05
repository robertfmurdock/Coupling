package com.zegreatrob.coupling.common.entity.tribe;

data class KtTribe(
        val id: TribeId,
        val pairingRule: PairingRule = PairingRule.LongestTime,
        val badgesEnabled: Boolean = false,
        val defaultBadgeName: String? = "Default",
        val alternateBadgeName: String? = "Alternate",
        val email: String? = null,
        val name: String? = null,
        val callSignsEnabled: Boolean = false
)