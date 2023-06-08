package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.CouplingSdk
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

class CommandDispatcher(override val sdk: CouplingSdk) :
    SuspendActionExecuteSyntax,
    NewPartyCommandDispatcher,
    ClientPartyPlayerQueryDispatcher,
    ClientStatisticsQueryDispatcher,
    CouplingSdk by sdk
