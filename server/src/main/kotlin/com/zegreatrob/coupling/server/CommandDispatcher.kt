package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinsQueryDispatcherJs
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcherJs
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcherJs
import com.zegreatrob.coupling.server.entity.user.UserDispatcherJs
import kotlinx.coroutines.*

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(
    jsRepository: dynamic,
    userCollection: dynamic,
    userEmail: String,
    tribeIds: Array<String>,
    path: String
): Any {
    val user = User(userEmail, tribeIds.map(::TribeId).toSet())
    return CommandDispatcher(user, jsRepository, userCollection, path)
}

class CommandDispatcher(override val user: User, jsRepository: dynamic, userCollection: dynamic, path: String) :
    TribeDispatcherJs,
    PlayerDispatcherJs,
    PairAssignmentDispatcherJs,
    UserDispatcherJs,
    HandleWebsocketConnectionActionDispatcher,
    RepositoryCatalog by MongoRepositoryCatalog(userCollection, jsRepository, user),
    PinDispatcherJs {
    override val scope = MainScope() + CoroutineName(path)

    private var authorizedTribeIdDispatcherJob: Deferred<AuthorizedTribeIdDispatcher>? = null

    @Suppress("unused")
    @JsName("authorizedDispatcher")
    fun authorizedDispatcher(tribeId: String) = scope.promise {
        val preexistingJob = authorizedTribeIdDispatcherJob
        if (preexistingJob == null) {
            val async = scope.async {
                AuthorizedTribeIdDispatcher(
                    TribeId(tribeId).validateAuthorized(), this@CommandDispatcher
                )
            }
            authorizedTribeIdDispatcherJob = async
            async.await()
        } else
            preexistingJob.await()
    }

    private suspend fun TribeId.validateAuthorized() =
        if (userIsAuthorized(this)) this else null

    private suspend fun userIsAuthorized(tribeId: TribeId) = UserIsAuthorizedAction(tribeId).perform()
}

class AuthorizedTribeIdDispatcher(
    override val authorizedTribeId: TribeId?,
    private val commandDispatcher: CommandDispatcher
) :
    RepositoryCatalog by commandDispatcher,
    ScopeSyntax by commandDispatcher,
    AuthenticatedUserSyntax by commandDispatcher,
    UserEmailSyntax by commandDispatcher,
    PinsQueryDispatcherJs