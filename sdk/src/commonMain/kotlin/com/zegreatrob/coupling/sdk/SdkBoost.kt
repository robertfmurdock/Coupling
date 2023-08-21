package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.json.ApplyBoostInput
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkBoost :
    GqlSyntax,
    ApplyBoostCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: ApplyBoostCommand) = doQuery(
        query = Mutation.saveBoost,
        input = command.saveBoostInput(),
    ).let { ApplyBoostCommand.Result.Unknown("whoops") }

    override suspend fun perform(command: DeleteBoostCommand) = deleteIt().let { VoidResult.Accepted }

    private fun ApplyBoostCommand.saveBoostInput() = ApplyBoostInput(partyId)

    private suspend fun deleteIt() {
        performQuery(buildJsonObject { put("query", Mutation.deleteBoost) })
    }
}
