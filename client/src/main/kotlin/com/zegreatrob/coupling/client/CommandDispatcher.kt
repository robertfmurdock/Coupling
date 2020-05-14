package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.MasterDispatcher
import com.zegreatrob.coupling.action.SuccessfulExecutableAction
import com.zegreatrob.coupling.action.SuspendAction
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

class CommandDispatcher(override val traceId: Uuid) : ActionLoggingSyntax,
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
    MasterDispatcher {
    override val sdk = SdkSingleton
    override val masterDispatcher = this

    override fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D) =
        command.log { command.execute(dispatcher) }.value

    override suspend fun <C : SuspendAction<D, R>, D, R> invoke(command: C, dispatcher: D) =
        command.logAsync { command.execute(dispatcher) }
}
