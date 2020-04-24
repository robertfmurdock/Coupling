package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json

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
        onMainScope(done) {
            authActionDispatcher(userId, uuid4())
                .findOrCreateUser()
        }
    }

    private fun onMainScope(done: (dynamic, dynamic) -> Unit, block: suspend CoroutineScope.() -> Json) {
        MainScope().promise(block = block).then({ done(null, it) }, { done(it, null) })
    }

    private suspend fun authActionDispatcher(userId: String, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId
    )

    fun findOrCreate(email: String, traceId: Uuid) = MainScope().promise {
        authActionDispatcher(email, traceId)
            .findOrCreateUser()
    }

    private suspend fun FindOrCreateUserActionDispatcher.findOrCreateUser() = FindOrCreateUserAction.perform()
        .toJson()
}
