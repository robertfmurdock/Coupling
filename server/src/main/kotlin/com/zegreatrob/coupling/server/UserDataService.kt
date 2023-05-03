package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid

object UserDataService {

    suspend fun authActionDispatcher(userId: String, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId,
    )
}
