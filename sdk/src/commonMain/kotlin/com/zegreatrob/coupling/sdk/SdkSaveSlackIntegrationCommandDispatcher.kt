package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SaveSlackIntegrationMutation
import com.zegreatrob.coupling.sdk.schema.type.SaveSlackIntegrationInput

interface SdkSaveSlackIntegrationCommandDispatcher :
    SaveSlackIntegrationCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SaveSlackIntegrationCommand) = SaveSlackIntegrationMutation(command.saveSlackIntegrationInput()).execute()
        .data
        ?.saveSlackIntegration
        .toVoidResult()

    private fun SaveSlackIntegrationCommand.saveSlackIntegrationInput() = SaveSlackIntegrationInput(
        team = team,
        channel = channel,
        partyId = partyId,
    )
}
