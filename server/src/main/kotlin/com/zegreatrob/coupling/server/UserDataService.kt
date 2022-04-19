package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.Done
import com.zegreatrob.coupling.server.external.express.Request

object UserDataService {

    fun serializeUser(user: User, done: Done) {
        done(null, user.id)
    }

    fun deserializeUser(request: Request, userId: String, done: Done) = request.scope.async(done) {
        authActionDispatcher(userId, uuid4())(FindOrCreateUserAction)
            .valueOrNull()
    }

    suspend fun authActionDispatcher(userId: String, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId
    )

    suspend fun findOrCreateUser(email: String, traceId: Uuid) =
        authActionDispatcher(email, traceId).invoke(FindOrCreateUserAction)
            .valueOrNull()
}
