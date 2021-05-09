package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax
import com.zegreatrob.coupling.server.action.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.action.player.*
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcher
import com.zegreatrob.coupling.server.entity.user.UserDispatcher
import com.zegreatrob.coupling.server.express.route.HandleWebsocketConnectionActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

interface ICommandDispatcher :
    LoggingActionExecuteSyntax,
    ScopeSyntax,
    TribeDispatcher,
    PairAssignmentDispatcher,
    UserDispatcher,
    HandleWebsocketConnectionActionDispatcher,
    DispatchingActionExecutor<CommandDispatcher>,
    RepositoryCatalog

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid
) : ICommandDispatcher, RepositoryCatalog by repositoryCatalog {
    override val execute = this
    override val actionDispatcher = this
    override val liveInfoRepository get() = MemoryLiveInfoRepository()

    private var authorizedTribeIdDispatcherJob: Deferred<CurrentTribeIdDispatcher>? = null

    suspend fun authorizedTribeIdDispatcher(tribeId: String): CurrentTribeIdDispatcher {
        val preexistingJob = authorizedTribeIdDispatcherJob
        return preexistingJob?.await()
            ?: scope.async {
                CurrentTribeIdDispatcher(TribeId(tribeId), this@CommandDispatcher)
            }.also {
                authorizedTribeIdDispatcherJob = it
            }.await()
    }

}

class CurrentTribeIdDispatcher(
    override val currentTribeId: TribeId,
    private val commandDispatcher: CommandDispatcher
) :
    ICommandDispatcher by commandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    RetiredPlayersQueryDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeleteTribeCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    ProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher {
    override val userId: String get() = commandDispatcher.userId

    suspend fun isAuthorized() = currentTribeId.validateAuthorized() != null

    private suspend fun TribeId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private fun nonCachingPlayerQueryDispatcher() = object : PlayersQueryDispatcher,
        LoggingActionExecuteSyntax by this,
        CurrentTribeIdSyntax by this,
        RepositoryCatalog by this {}

    private val playerDeferred = scope.async(start = CoroutineStart.LAZY) {
        with(nonCachingPlayerQueryDispatcher()) {
            perform(PlayersQuery)
        }
    }

    override suspend fun perform(query: PlayersQuery) = playerDeferred.await()

    private suspend fun userIsAuthorized(tribeId: TribeId) = user.authorizedTribeIds.contains(tribeId)
            || userIsAlsoPlayer()

    private suspend fun userIsAlsoPlayer() = players()
        .map { it.email }
        .contains(user.email)

    private suspend fun players() = playerDeferred.await().value.map { it.data.element }

}
