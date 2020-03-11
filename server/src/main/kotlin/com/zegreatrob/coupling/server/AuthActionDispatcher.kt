package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.entity.user.AuthUserDispatcherJs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

@Suppress("unused")
@JsName("authActionDispatcher")
fun authActionDispatcher(userCollection: dynamic, userEmail: String, traceId: Uuid?) = GlobalScope.promise {
    AuthActionDispatcher(
        userEmail,
        userRepository(userCollection, userEmail),
        traceId
    )
}

@Suppress("unused")
class AuthActionDispatcher(
    override val userEmail: String,
    override val userRepository: UserRepository,
    override val traceId: Uuid?
) : AuthUserDispatcherJs, UserRepository by userRepository {
    override val scope: CoroutineScope = MainScope()
}
