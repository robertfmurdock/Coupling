package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.Done
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

object UserDataService {

    fun serializeUser(user: User, done: Done) {
        done(null, user.id)
    }

    fun deserializeUser(request: Request, userId: String, done: Done): Unit = request.scope.async(done) {
        authActionDispatcher(userId, uuid4())
            .findOrCreateUser()
    }

    private suspend fun authActionDispatcher(userId: String, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId
    )

    fun findOrCreate(email: String, traceId: Uuid): Promise<User> = MainScope().promise {
        authActionDispatcher(email, traceId)
            .findOrCreateUser()
    }

    private suspend fun FindOrCreateUserActionDispatcher.findOrCreateUser() = FindOrCreateUserAction.perform()
}
