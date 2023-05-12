package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.SdkApi

class CommandDispatcher(private val sdk: SdkApi) :
    NewPartyCommandDispatcher,
    SdkApi by sdk
