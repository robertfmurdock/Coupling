package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.axios.axios
import kotlinx.coroutines.await

object LogoutCommand

interface LogoutCommandDispatcher {
    suspend fun LogoutCommand.perform() = axios.get("/api/logout").await().unsafeCast<Unit>()
}
