package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

object UserDataService {
    fun serializeUser(user: dynamic, done: (dynamic, dynamic) -> Unit) {
        val userId = user.id.unsafeCast<Any?>()
        if (userId != null) {
            done(null, userId)
        } else {
            done("The user did not have an id to serialize.", null)
        }
    }

    fun deserializeUser(userId: String, done: (dynamic, dynamic) -> Unit) {
        GlobalScope.promise {
            with(AuthActionDispatcher(userId, userRepository(userId), uuid4())) {
                FindOrCreateUserAction.perform()
                    .toJson()
            }
        }.then({ done(null, it) }, { done(it, null) })
    }

    suspend fun findOrCreate(email: String, traceId: Uuid) =
        with(AuthActionDispatcher(email, userRepository(email), traceId)) {
            FindOrCreateUserAction.perform()
                .toJson()
        }
}
