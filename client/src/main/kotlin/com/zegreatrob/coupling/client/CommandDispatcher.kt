package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.BarebonesSdk

class CommandDispatcher(private val sdk: BarebonesSdk) :
    NewPartyCommandDispatcher,
    BarebonesSdk by sdk
