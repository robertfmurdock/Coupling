package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pairassignments.SavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.player.TribePlayerQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.client.tribe.TribeListQueryDispatcher
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton

object CommandDispatcher : PinCommandDispatcher,
    SavePairAssignmentsCommandDispatcher,
    NewPairAssignmentsQueryDispatcher,
    PlayerConfigDispatcher,
    TribeConfigDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    RepositoryCatalog by SdkSingleton,
    NullTraceIdProvider,
    TribeDataSetQueryDispatcher,
    HistoryQueryDispatcher,
    RetiredPlayerQueryDispatcher,
    RetiredPlayerListQueryDispatcher,
    TribeListQueryDispatcher,
    TribePlayerQueryDispatcher {
    override val sdk = SdkSingleton
}
