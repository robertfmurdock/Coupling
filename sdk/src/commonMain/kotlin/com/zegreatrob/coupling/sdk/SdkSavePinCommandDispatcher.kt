package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SavePartyMutation

interface SdkSavePinCommandDispatcher :
    SavePinCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePinCommand): VoidResult.Accepted {
        SavePartyMutation(
            SavePartyCommand(
                partyId = command.id,
                pins = listOf(command.pin),
            ).savePartyInput(),
        ).execute()
        return VoidResult.Accepted
    }
}
