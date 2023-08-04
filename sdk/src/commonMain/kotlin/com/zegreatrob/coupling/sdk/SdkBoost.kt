package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkBoost :
    GqlSyntax,
    SaveBoostCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = doQuery(
        query = Mutation.saveBoost,
        input = command.saveBoostInput(),
    ).let { VoidResult.Accepted }

    override suspend fun perform(command: DeleteBoostCommand) = deleteIt().let { VoidResult.Accepted }

    private fun SaveBoostCommand.saveBoostInput() = SaveBoostInput(partyIds)

    private suspend fun deleteIt() {
        performQuery(buildJsonObject { put("query", Mutation.deleteBoost) })
    }
}
