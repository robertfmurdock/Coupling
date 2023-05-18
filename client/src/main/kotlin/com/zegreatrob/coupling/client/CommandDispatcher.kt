package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientNewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientPartyPlayerQueryDispatcher
import com.zegreatrob.coupling.sdk.ClientRetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.sdk.ClientStatisticsQueryDispatcher
import com.zegreatrob.coupling.sdk.SdkApi

class CommandDispatcher(override val sdk: SdkApi) :
    NewPartyCommandDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    ClientPartyPlayerQueryDispatcher,
    ClientRetiredPlayerQueryDispatcher,
    ClientStatisticsQueryDispatcher,
    SdkApi by sdk
