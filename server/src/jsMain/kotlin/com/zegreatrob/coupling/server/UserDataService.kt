package com.zegreatrob.coupling.server

import kotlin.uuid.Uuid

object UserDataService {

    suspend fun authActionDispatcher(userId: String, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId,
    )
}
