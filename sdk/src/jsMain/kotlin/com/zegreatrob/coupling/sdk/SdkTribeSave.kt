package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSave
import kotlinx.serialization.json.encodeToDynamic
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

    private fun Tribe.saveTribeInput() = couplingJsonFormat.encodeToDynamic(
        SaveTribeInput(
            tribeId = id.value,
            pairingRule = PairingRule.toValue(pairingRule),
            name = name,
            email = email,
            defaultBadgeName = defaultBadgeName,
            alternateBadgeName = alternateBadgeName,
            badgesEnabled = badgesEnabled,
            callSignsEnabled = callSignsEnabled,
            animationsEnabled = animationEnabled,
            animationSpeed = animationSpeed
        )
    )
}
