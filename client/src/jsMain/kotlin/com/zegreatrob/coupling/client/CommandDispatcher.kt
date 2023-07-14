package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.client.memory.ClientPartyPlayerQueryDispatcher
import com.zegreatrob.coupling.client.memory.ClientStatisticsQueryDispatcher
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.CouplingSdk

class CommandDispatcher(override val traceId: Uuid, override val sdk: CouplingSdk) :
    TraceIdProvider,
    NewPartyCommandDispatcher,
    ClientPartyPlayerQueryDispatcher,
    ClientStatisticsQueryDispatcher,
    CouplingSdk by sdk
