package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyConfigDispatcher
import com.zegreatrob.coupling.client.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.client.party.PartyQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.PartyPinQueryDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.player.PartyPlayerQueryDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.StatisticsQueryDispatcher
import com.zegreatrob.coupling.sdk.BarebonesSdk

class CommandDispatcher(override val traceId: Uuid, override val sdk: BarebonesSdk) :
    PinCommandDispatcher,
    PairAssignmentsCommandDispatcher,
    NewPairAssignmentsCommandDispatcher,
    PlayerConfigDispatcher,
    PartyConfigDispatcher,
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
    StatisticsQueryDispatcher,
    LoggingActionExecuteSyntax,
    BarebonesSdk by sdk
