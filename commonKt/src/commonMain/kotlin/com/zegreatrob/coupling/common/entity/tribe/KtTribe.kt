package com.zegreatrob.coupling.common.entity.tribe;

data class KtTribe(
        val id: TribeId,
        val pairingRule: PairingRule = PairingRule.LongestTime,
        val defaultBadgeName: String? = "Default",
        val alternateBadgeName: String? = "Alternate",
        val name: String? = null
)