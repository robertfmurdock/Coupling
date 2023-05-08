package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.ClientSavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.ClientDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.party.ClientDeletePartyCommandDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.client.party.PartyQueryDispatcher
import com.zegreatrob.coupling.client.pin.ClientDeletePinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.ClientStatisticsQueryDispatcher
import com.zegreatrob.coupling.sdk.BarebonesSdk
import com.zegreatrob.coupling.sdk.ClientDeletePlayerCommandDispatcher

class CommandDispatcher(private val sdk: BarebonesSdk) :
    ClientDeletePairAssignmentsCommandDispatcher,
    ClientDeletePartyCommandDispatcher,
    ClientDeletePinCommandDispatcher,
    ClientDeletePlayerCommandDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    ClientSavePairAssignmentsCommandDispatcher,
    HistoryQueryDispatcher,
    NewPairAssignmentsCommandDispatcher,
    NewPartyCommandDispatcher,
    PartyCurrentDataQueryDispatcher,
    PartyListQueryDispatcher,
    PartyPinListQueryDispatcher,
    PartyPinQueryDispatcher,
    PartyQueryDispatcher,
    RetiredPlayerListQueryDispatcher,
    RetiredPlayerQueryDispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    ClientStatisticsQueryDispatcher,
    BarebonesSdk by sdk
