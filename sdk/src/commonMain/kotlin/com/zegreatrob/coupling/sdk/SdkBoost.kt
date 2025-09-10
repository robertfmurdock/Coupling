package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeleteBoostMutation
import com.zegreatrob.coupling.sdk.schema.SaveBoostMutation
import com.zegreatrob.coupling.sdk.schema.type.SaveBoostInput

interface SdkBoost :
    GqlTrait,
    ApplyBoostCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: ApplyBoostCommand): ApplyBoostCommand.Result.Unknown = SaveBoostMutation(
        command.saveBoostInput(),
    ).execute()
        .let { ApplyBoostCommand.Result.Unknown("whoops") }

    override suspend fun perform(command: DeleteBoostCommand) = deleteIt().let { VoidResult.Accepted }

    private fun ApplyBoostCommand.saveBoostInput() = SaveBoostInput(partyId)

    private suspend fun deleteIt() {
        DeleteBoostMutation().execute()
    }
}
