package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.SavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQueryDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryQueryDispatcher
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.pin.TribePinListQueryDispatcher
import com.zegreatrob.coupling.client.pin.TribePinQueryDispatcher
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.player.TribePlayerQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerListQueryDispatcher
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerQueryDispatcher
import com.zegreatrob.coupling.client.stats.StatisticsQueryDispatcher
import com.zegreatrob.coupling.client.tribe.NewTribeCommandDispatcher
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.client.tribe.TribeListQueryDispatcher
import com.zegreatrob.coupling.client.tribe.TribeQueryDispatcher
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import com.zegreatrob.coupling.client.user.LogoutCommandDispatcher
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton

class CommandDispatcher(override val traceId: Uuid) :
    PinCommandDispatcher,
    SavePairAssignmentsCommandDispatcher,
    NewPairAssignmentsQueryDispatcher,
    PlayerConfigDispatcher,
    TribeConfigDispatcher,
    DeletePairAssignmentsCommandDispatcher,
    RepositoryCatalog by SdkSingleton,
    TribeDataSetQueryDispatcher,
    HistoryQueryDispatcher,
    RetiredPlayerQueryDispatcher,
    RetiredPlayerListQueryDispatcher,
    TribeListQueryDispatcher,
    TribeQueryDispatcher,
    TribePlayerQueryDispatcher,
    TribePinQueryDispatcher,
    TribePinListQueryDispatcher,
    GoogleSignInCommandDispatcher,
    LogoutCommandDispatcher,
    NewTribeCommandDispatcher,
    StatisticsQueryDispatcher,
    LoggingActionExecuteSyntax {
    override val sdk = SdkSingleton
}
