package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSave

interface SdkTribeSave : TribeSave, GqlSyntax {
    override suspend fun save(tribe: Tribe) {
        doQuery(Mutations.saveTribe, tribe.saveTribeInput())
    }

    private fun Tribe.saveTribeInput() = SaveTribeInput(
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
}
