package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.user.UserId
import kotlin.uuid.Uuid

object UserDataService {

    suspend fun authActionDispatcher(userId: UserId, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId,
    )
}
