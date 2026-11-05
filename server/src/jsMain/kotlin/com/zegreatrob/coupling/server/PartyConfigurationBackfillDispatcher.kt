package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.repository.dynamo.DynamoPartyConfigurationBackfill
import com.zegreatrob.coupling.server.action.party.BackfillPartyConfigurationCommand

class PartyConfigurationBackfillDispatcher : BackfillPartyConfigurationCommand.Dispatcher {
    override val partyConfigurationBackfill = DynamoPartyConfigurationBackfill()
}
