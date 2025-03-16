package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import kotlin.uuid.Uuid

class AuthActionDispatcher internal constructor(
    override val userId: UserId,
    override val userRepository: UserRepository,
    override val traceId: Uuid,
) : TraceIdProvider,
    FindOrCreateUserActionDispatcher,
    UserRepository by userRepository
