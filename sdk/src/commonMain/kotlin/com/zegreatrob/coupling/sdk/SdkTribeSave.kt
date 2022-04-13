package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.tribe.TribeSave

interface SdkTribeSave : TribeSave, GqlSyntax, GraphQueries {
    override suspend fun save(tribe: Party) {
        doQuery(mutations.saveTribe, tribe.saveTribeInput())
    }

    private fun Party.saveTribeInput() = SaveTribeInput(
        tribeId = id,
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
