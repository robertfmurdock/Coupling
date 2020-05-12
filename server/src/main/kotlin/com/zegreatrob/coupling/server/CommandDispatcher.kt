package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcher
import com.zegreatrob.coupling.server.entity.pin.PinDispatcher
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcher
import com.zegreatrob.coupling.server.entity.user.UserDispatcher
import com.zegreatrob.coupling.server.express.route.HandleWebsocketConnectionActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid
) :
    ScopeSyntax,
    TribeDispatcher,
    PlayerDispatcher,
    PairAssignmentDispatcher,
    UserDispatcher,
    HandleWebsocketConnectionActionDispatcher,
    RepositoryCatalog by repositoryCatalog,
    PinDispatcher {

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
    RepositoryCatalog by commandDispatcher,
    ScopeSyntax by commandDispatcher,
    AuthenticatedUserSyntax by commandDispatcher,
    UserEmailSyntax by commandDispatcher,
    TraceIdSyntax by commandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    UserIsAuthorizedActionDispatcher {

    suspend fun isAuthorized() = currentTribeId.validateAuthorized() != null

    private suspend fun TribeId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private fun nonCachingPlayerQueryDispatcher() = object : PlayersQueryDispatcher,
        TraceIdSyntax by this,
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
