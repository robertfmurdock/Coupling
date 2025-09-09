package com.zegreatrob.coupling.sdk

import com.example.DeleteBoostMutation
import com.example.SaveBoostMutation
import com.example.type.SaveBoostInput
import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkBoost :
    GqlTrait,
    ApplyBoostCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: ApplyBoostCommand): ApplyBoostCommand.Result.Unknown = apolloMutation(SaveBoostMutation(command.saveBoostInput()))
        .let { ApplyBoostCommand.Result.Unknown("whoops") }

    override suspend fun perform(command: DeleteBoostCommand) = deleteIt().let { VoidResult.Accepted }

    private fun ApplyBoostCommand.saveBoostInput() = SaveBoostInput(partyId)

    private suspend fun deleteIt() {
        apolloMutation(DeleteBoostMutation())
    }
}
