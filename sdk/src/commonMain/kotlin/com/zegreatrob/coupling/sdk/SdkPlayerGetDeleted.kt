package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.sdk.PartyGQLComponent.RetiredPlayerList

interface SdkPlayerGetDeleted : PlayerListGetDeleted, GqlQueryComponent {
    override suspend fun getDeleted(partyId: PartyId) = performQueryGetComponent(partyId, RetiredPlayerList, ::toModel)
        ?: emptyList()

    private fun toModel(it: List<JsonPlayerRecord>) = it.map(JsonPlayerRecord::toModel)
}
