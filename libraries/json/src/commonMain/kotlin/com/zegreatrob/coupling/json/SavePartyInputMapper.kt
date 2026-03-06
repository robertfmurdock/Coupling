package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.action.party.SavePartyCommand

fun GqlSavePartyInput.toCommand() = SavePartyCommand(
    partyId = partyId,
    party = party?.toModel(partyId),
    players = players?.items?.map { it.toModel() }.orEmpty(),
    pins = pins?.items?.map { it.toModel() }.orEmpty(),
)
