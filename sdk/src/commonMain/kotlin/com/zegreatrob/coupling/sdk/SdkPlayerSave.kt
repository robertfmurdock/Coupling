package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.partyId
import com.zegreatrob.coupling.repository.player.PlayerSave

interface SdkPlayerSave : PlayerSave, GqlSyntax, GraphQueries {
    override suspend fun save(partyPlayer: PartyElement<Player>) {
        doQuery(mutations.savePlayer, partyPlayer.input())
    }

    private fun PartyElement<Player>.input() =
        SavePlayerInput(
            tribeId = partyId,
            playerId = element.id,
            name = element.name,
            email = element.email,
            badge = "${element.badge}",
            callSignAdjective = element.callSignAdjective,
            callSignNoun = element.callSignNoun,
            imageURL = element.imageURL,
        )
}
