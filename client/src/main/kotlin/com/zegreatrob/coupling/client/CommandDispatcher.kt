package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pairassignments.SavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton

object CommandDispatcher : PinCommandDispatcher,
    SavePairAssignmentsCommandDispatcher,
    PlayerConfigDispatcher,
    TribeConfigDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    RepositoryCatalog by SdkSingleton,
    NullTraceIdProvider,
    TribeDataSetQueryDispatcher
