package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.example.SavePlayerMutation
import com.example.type.SavePlayerInput
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePlayerCommand) = with(command) {
        apolloMutation(SavePlayerMutation(partyId.with(player).input()))
        VoidResult.Accepted
    }
}

fun PartyElement<Player>.input() = SavePlayerInput(
    partyId = partyId,
    playerId = element.id,
    name = element.name,
    email = element.email,
    badge = element.badge.toSerializable(),
    callSignAdjective = element.callSignAdjective,
    callSignNoun = element.callSignNoun,
    imageURL = presentIfNotNull(element.imageURL),
    avatarType = presentIfNotNull(element.avatarType?.name),
    unvalidatedEmails = element.additionalEmails.toList(),
)
