package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.server.entity.user.AuthUserDispatcherJs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@Suppress("unused")
@JsName("authActionDispatcher")
fun authActionDispatcher(userCollection: dynamic, userEmail: String) = AuthActionDispatcher(
    userEmail,
    userCollection
)

@Suppress("unused")
class AuthActionDispatcher(
    override val userEmail: String,
    override val userCollection: dynamic
) : AuthUserDispatcherJs, MongoUserRepository {
    override val userRepository = this
    override val scope: CoroutineScope = MainScope()
}
