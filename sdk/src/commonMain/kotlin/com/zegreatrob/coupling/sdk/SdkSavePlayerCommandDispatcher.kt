package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SavePartyMutation

interface SdkSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePlayerCommand): VoidResult.Accepted {
        SavePartyMutation(
            SavePartyCommand(
                partyId = command.partyId,
                players = listOf(command.player),
            ).savePartyInput(),
        ).execute()
        return VoidResult.Accepted
    }
}
