package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton

object CommandDispatcher : PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    RepositoryCatalog by SdkSingleton,
    NullTraceIdProvider
