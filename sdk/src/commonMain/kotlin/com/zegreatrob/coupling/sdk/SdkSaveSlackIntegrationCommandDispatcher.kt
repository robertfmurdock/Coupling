package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.json.GqlSaveSlackIntegrationInput
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSaveSlackIntegrationCommandDispatcher :
    SaveSlackIntegrationCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SaveSlackIntegrationCommand) = doQuery(Mutation.saveSlackIntegration, command.saveSlackIntegrationInput())
        .parseMutationResult()
        .toDomain()
        .saveSlackIntegration
        .toVoidResult()

    private fun SaveSlackIntegrationCommand.saveSlackIntegrationInput() = GqlSaveSlackIntegrationInput(
        team = team,
        channel = channel,
        partyId = partyId.value,
    )
}
