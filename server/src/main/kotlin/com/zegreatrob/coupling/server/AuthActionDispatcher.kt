package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.entity.user.AuthUserDispatcherJs
import kotlinx.coroutines.CoroutineScope

class AuthActionDispatcher(
    override val userId: String,
    override val userRepository: UserRepository,
    override val traceId: Uuid,
    override val scope: CoroutineScope
) : AuthUserDispatcherJs, UserRepository by userRepository {
}
