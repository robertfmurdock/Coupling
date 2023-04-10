package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.action.RequestSpinAction
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.components.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.ClientSavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.ClientDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.party.ClientDeletePartyCommandDispatcher
import com.zegreatrob.coupling.client.party.ClientSavePartyCommandDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.client.party.PartyQueryDispatcher
import com.zegreatrob.coupling.client.pin.ClientDeletePinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinQueryDispatcher
import com.zegreatrob.coupling.client.player.ClientDeletePlayerCommandDispatcher
import com.zegreatrob.coupling.client.player.ClientSavePlayerCommandDispatcher
import com.zegreatrob.coupling.client.player.PartyPlayerQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.StatisticsQueryDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.sdk.BarebonesSdk

class CommandDispatcher(override val traceId: Uuid, override val sdk: BarebonesSdk) :
    ClientDeletePairAssignmentsCommandDispatcher,
    ClientDeletePartyCommandDispatcher,
    ClientDeletePinCommandDispatcher,
    ClientDeletePlayerCommandDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    ClientSavePairAssignmentsCommandDispatcher,
    ClientSavePartyCommandDispatcher,
    ClientSavePlayerCommandDispatcher,
    HistoryQueryDispatcher,
    LoggingActionExecuteSyntax,
    NewPairAssignmentsCommandDispatcher,
    NewPartyCommandDispatcher,
    PartyCurrentDataQueryDispatcher,
    PartyListQueryDispatcher,
    PartyPinListQueryDispatcher,
    PartyPinQueryDispatcher,
    PartyPlayerQueryDispatcher,
    PartyQueryDispatcher,
    RetiredPlayerListQueryDispatcher,
    RetiredPlayerQueryDispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    SavePinCommandDispatcher,
    SdkRequestSpinActionDispatcher,
    StatisticsQueryDispatcher,
    BarebonesSdk by sdk {
    override suspend fun perform(action: RequestSpinAction): PairAssignmentDocument = sdk.perform(action)
}
