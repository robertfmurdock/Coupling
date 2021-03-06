package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.TribeCurrentDataQueryDispatcher
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

class CommandDispatcher(override val traceId: Uuid, repositoryCatalog: RepositoryCatalog) :
    PinCommandDispatcher,
    PairAssignmentsCommandDispatcher,
    NewPairAssignmentsCommandDispatcher,
    PlayerConfigDispatcher,
    TribeConfigDispatcher,
    RepositoryCatalog by repositoryCatalog,
    TribeCurrentDataQueryDispatcher,
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
