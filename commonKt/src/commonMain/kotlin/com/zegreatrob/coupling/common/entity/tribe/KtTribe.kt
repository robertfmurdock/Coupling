package com.zegreatrob.coupling.common.entity.tribe;

data class KtTribe(
        val id: TribeId,
        val pairingRule: PairingRule,
        val defaultBadgeName: String? = null,
        val alternateBadgeName: String? = null,
        val name: String? = null
)