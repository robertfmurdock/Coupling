package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcherJs
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDocumentListQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinsQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcherJs
import com.zegreatrob.coupling.server.entity.player.PlayersQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcherJs
import com.zegreatrob.coupling.server.entity.user.UserDispatcherJs
import com.zegreatrob.coupling.server.entity.user.toUser
import kotlinx.coroutines.*
import kotlin.js.Json

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(
    jsRepository: dynamic,
    userCollection: dynamic,
    userJson: Json,
    path: String,
    traceId: Uuid?
): Any {
    val user = userJson.toUser()
    val scope = MainScope() + CoroutineName(path)
    return scope.promise { commandDispatcher(userCollection, jsRepository, user, scope, traceId) }
}

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid?
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
    PlayersQueryDispatcherJs,
    PairAssignmentDocumentListQueryDispatcherJs,
    UserIsAuthorizedActionDispatcher {

    suspend fun isAuthorized() = currentTribeId.validateAuthorized() != null

    private suspend fun TribeId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private suspend fun userIsAuthorized(tribeId: TribeId) = UserIsAuthorizedAction(tribeId).perform()

}