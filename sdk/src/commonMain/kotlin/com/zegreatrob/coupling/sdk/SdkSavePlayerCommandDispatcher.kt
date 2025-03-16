package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.json.GqlSavePlayerInput
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePlayerCommand) = with(command) {
        doQuery(Mutation.savePlayer, partyId.with(player).input())
        VoidResult.Accepted
    }
}

fun PartyElement<Player>.input() = GqlSavePlayerInput(
    partyId = partyId.value,
    playerId = element.id.value.toString(),
    name = element.name,
    email = element.email,
    badge = "${element.badge}",
    callSignAdjective = element.callSignAdjective,
    callSignNoun = element.callSignNoun,
    imageURL = element.imageURL,
    avatarType = element.avatarType?.name,
    unvalidatedEmails = element.additionalEmails.toList(),
)
