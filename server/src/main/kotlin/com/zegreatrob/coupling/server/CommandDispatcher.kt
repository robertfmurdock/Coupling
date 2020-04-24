package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdSyntax
import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.json.toUser
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcherJs
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDocumentListQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinsQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcherJs
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcherJs
import com.zegreatrob.coupling.server.entity.user.UserDispatcherJs
import kotlinx.coroutines.*
import kotlin.js.Json
import kotlin.js.Promise

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(
    userJson: Json,
    path: String,
    traceId: Uuid
): Promise<CommandDispatcher> {
    val user = userJson.toUser()
    val scope = MainScope() + CoroutineName(path)
    return scope.promise { commandDispatcher(user, scope, traceId) }
}

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid
) :
    TribeDispatcherJs,
    PlayerDispatcherJs,
    PairAssignmentDispatcherJs,
    UserDispatcherJs,
    HandleWebsocketConnectionActionDispatcher,
    RepositoryCatalog by repositoryCatalog,
    PinDispatcherJs {

    private var authorizedTribeIdDispatcherJob: Deferred<CurrentTribeIdDispatcher>? = null

    @Suppress("unused")
    @JsName("authorizedDispatcher")
    fun authorizedDispatcher(tribeId: String) = scope.promise {
        authorizedTribeIdDispatcher(tribeId)
    }

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
    PinsQueryDispatcherJs,
    PlayersQueryDispatcher,
    PairAssignmentDocumentListQueryDispatcherJs,
    UserIsAuthorizedActionDispatcher {

    suspend fun isAuthorized() = currentTribeId.validateAuthorized() != null

    private suspend fun TribeId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private val playerDeferred = scope.async(start = CoroutineStart.LAZY) { PlayersQuery.perform() }

    suspend fun performPlayerListQueryGQL() = playerDeferred.await().toJsonArray()

    private suspend fun userIsAuthorized(tribeId: TribeId) = user.authorizedTribeIds.contains(tribeId)
            || userIsAlsoPlayer()

    private suspend fun userIsAlsoPlayer() = players()
        .map { it.email }
        .contains(user.email)

    private suspend fun players() = playerDeferred.await().map { it.data.element }

}