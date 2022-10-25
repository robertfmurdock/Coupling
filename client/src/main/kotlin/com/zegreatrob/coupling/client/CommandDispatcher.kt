package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.action.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.SavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.party.DeletePartyCommandDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.client.party.PartyQueryDispatcher
import com.zegreatrob.coupling.client.party.SavePartyCommandDispatcher
import com.zegreatrob.coupling.client.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinQueryDispatcher
import com.zegreatrob.coupling.client.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.client.player.PartyPlayerQueryDispatcher
import com.zegreatrob.coupling.client.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.StatisticsQueryDispatcher
import com.zegreatrob.coupling.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.components.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.sdk.BarebonesSdk

class CommandDispatcher(override val traceId: Uuid, override val sdk: BarebonesSdk) :
    SavePinCommandDispatcher,
    DeletePinCommandDispatcher,
    NewPairAssignmentsCommandDispatcher,
    SavePairAssignmentsCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    SavePartyCommandDispatcher,
    DeletePartyCommandDispatcher,
    PartyCurrentDataQueryDispatcher,
    HistoryQueryDispatcher,
    RetiredPlayerQueryDispatcher,
    RetiredPlayerListQueryDispatcher,
    PartyListQueryDispatcher,
    PartyQueryDispatcher,
    PartyPlayerQueryDispatcher,
    PartyPinQueryDispatcher,
    PartyPinListQueryDispatcher,
    NewPartyCommandDispatcher,
    SdkRequestSpinActionDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    StatisticsQueryDispatcher,
    LoggingActionExecuteSyntax,
    BarebonesSdk by sdk {
    override suspend fun perform(action: RequestSpinAction): PairAssignmentDocument = sdk.perform(action)
}
