package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSave
import kotlin.js.json

interface SdkTribeSave : TribeSave, GqlSyntax {
    override suspend fun save(tribe: Tribe) {
        performQuery(
            json(
                "query" to Mutations.saveTribe,
                "variables" to json(
                    "input" to tribe.saveTribeInput()
                )
            )
        )
    }

    private fun Tribe.saveTribeInput() = json(
        "tribeId" to id.value,
        "pairingRule" to PairingRule.toValue(pairingRule),
        "name" to name,
        "email" to email,
        "defaultBadgeName" to defaultBadgeName,
        "alternateBadgeName" to alternateBadgeName,
        "badgesEnabled" to badgesEnabled,
        "callSignsEnabled" to callSignsEnabled,
        "animationsEnabled" to animationEnabled,
        "animationSpeed" to animationSpeed
    )
}
