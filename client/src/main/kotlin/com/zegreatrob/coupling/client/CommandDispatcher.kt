package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.CouplingSdk

class CommandDispatcher(override val sdk: CouplingSdk) :
    NewPartyCommandDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    ClientPartyPlayerQueryDispatcher,
    ClientStatisticsQueryDispatcher,
    CouplingSdk by sdk
