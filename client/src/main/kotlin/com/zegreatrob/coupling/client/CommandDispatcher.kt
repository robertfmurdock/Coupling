package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton

object CommandDispatcher : PinCommandDispatcher, RepositoryCatalog by SdkSingleton, NullTraceIdProvider