package com.zegreatrob.coupling.server

import kotools.types.text.NotBlankString
import kotlin.uuid.Uuid

object UserDataService {

    suspend fun authActionDispatcher(userId: NotBlankString, traceId: Uuid) = AuthActionDispatcher(
        userId,
        userRepository(userId),
        traceId,
    )
}
