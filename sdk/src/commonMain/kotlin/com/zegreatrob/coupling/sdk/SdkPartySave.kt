package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartySave

interface SdkPartySave : PartySave, GqlSyntax, GraphQueries {
    override suspend fun save(party: Party) {
        doQuery(mutations.saveTribe, party.saveTribeInput())
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
