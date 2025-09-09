package com.zegreatrob.coupling.sdk

import com.example.SaveSlackIntegrationMutation
import com.example.type.SaveSlackIntegrationInput
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkSaveSlackIntegrationCommandDispatcher :
    SaveSlackIntegrationCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SaveSlackIntegrationCommand) = apolloMutation(SaveSlackIntegrationMutation(command.saveSlackIntegrationInput()))
        .data
        ?.saveSlackIntegration
        .toVoidResult()

    private fun SaveSlackIntegrationCommand.saveSlackIntegrationInput() = SaveSlackIntegrationInput(
        team = team,
        channel = channel,
        partyId = partyId,
    )
}
