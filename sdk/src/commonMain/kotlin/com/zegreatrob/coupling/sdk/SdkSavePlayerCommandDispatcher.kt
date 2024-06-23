package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    GqlSyntax {
    override suspend fun perform(command: SavePlayerCommand) = with(command) {
        doQuery(Mutation.savePlayer, partyId.with(player).input())
        VoidResult.Accepted
    }
}

fun PartyElement<Player>.input() = SavePlayerInput(
    partyId = partyId,
    playerId = element.id,
    name = element.name,
    email = element.email,
    badge = "${element.badge}",
    callSignAdjective = element.callSignAdjective,
    callSignNoun = element.callSignNoun,
    imageURL = element.imageURL,
    avatarType = element.avatarType?.name,
    unvalidatedEmails = element.additionalEmails,
)
