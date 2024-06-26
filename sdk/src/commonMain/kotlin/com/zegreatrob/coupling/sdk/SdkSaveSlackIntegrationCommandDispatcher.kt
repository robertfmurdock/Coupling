package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.json.SaveSlackIntegrationInput
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSaveSlackIntegrationCommandDispatcher :
    SaveSlackIntegrationCommand.Dispatcher,
    GqlSyntax {

    override suspend fun perform(command: SaveSlackIntegrationCommand) =
        doQuery(Mutation.saveSlackIntegration, command.saveSlackIntegrationInput())
            .parseMutationResult()
            .toDomain()
            .saveSlackIntegration
            .toVoidResult()

    private fun SaveSlackIntegrationCommand.saveSlackIntegrationInput() = SaveSlackIntegrationInput(
        team = team,
        channel = channel,
        partyId = partyId.value,
    )
}
