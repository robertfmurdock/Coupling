package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SavePlayerMutation
import com.zegreatrob.coupling.sdk.schema.type.SavePlayerInput

interface SdkSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePlayerCommand) = with(command) {
        SavePlayerMutation(partyId.with(player).input()).execute()
        VoidResult.Accepted
    }
}

internal fun PartyElement<Player>.input() = SavePlayerInput(
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
