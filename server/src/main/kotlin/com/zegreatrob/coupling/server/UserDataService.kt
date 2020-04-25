package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.Done
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

object UserDataService {

    fun serializeUser(user: User, done: Done) {
        done(null, user.id)
    }

    fun deserializeUser(request: Request, userId: String, done: Done): Unit = request.scope.async(done) {
        authActionDispatcher(userId, uuid4(), request.scope)
            .findOrCreateUser()
    }

    private suspend fun authActionDispatcher(userId: String, traceId: Uuid, scope: CoroutineScope) =
        AuthActionDispatcher(
            userId,
            userRepository(userId),
            traceId,
            scope
        )

    fun findOrCreate(email: String, traceId: Uuid, scope: CoroutineScope): Promise<User> = scope.promise {
        authActionDispatcher(email, traceId, scope)
            .findOrCreateUser()
    }

    private suspend fun FindOrCreateUserActionDispatcher.findOrCreateUser() = FindOrCreateUserAction.perform()
}
