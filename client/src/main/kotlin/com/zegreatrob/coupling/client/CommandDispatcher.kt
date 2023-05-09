package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.client.party.PartyQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.ClientStatisticsQueryDispatcher
import com.zegreatrob.coupling.sdk.BarebonesSdk

class CommandDispatcher(private val sdk: BarebonesSdk) :
    ClientNewPairAssignmentsCommandDispatcher,
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
    ClientStatisticsQueryDispatcher,
    BarebonesSdk by sdk
