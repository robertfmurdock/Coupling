package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
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
    path: String
): Any {
    val user = userJson.toUser()
    val scope = MainScope() + CoroutineName(path)
    return scope.promise { commandDispatcher(userCollection, jsRepository, user, scope) }
}

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope
) :
    TribeDispatcherJs,
    PlayerDispatcherJs,
    PairAssignmentDispatcherJs,
    UserDispatcherJs,
    HandleWebsocketConnectionActionDispatcher,
    RepositoryCatalog by repositoryCatalog,
    PinDispatcherJs {

    private var authorizedTribeIdDispatcherJob: Deferred<AuthorizedTribeIdDispatcher>? = null

    @Suppress("unused")
    @JsName("authorizedDispatcher")
    fun authorizedDispatcher(tribeId: String) = scope.promise {
        authorizedTribeIdDispatcher(tribeId)
    }

    suspend fun authorizedTribeIdDispatcher(tribeId: String): AuthorizedTribeIdDispatcher {
        val preexistingJob = authorizedTribeIdDispatcherJob
        return if (preexistingJob == null) {
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
    PinsQueryDispatcherJs,
    PlayersQueryDispatcherJs,
    PairAssignmentDocumentListQueryDispatcherJs