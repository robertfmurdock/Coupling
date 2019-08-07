package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import kotlinx.coroutines.await

interface ServerLogout {
    suspend fun logout() = axios.get("/api/logout").await().unsafeCast<Unit>()
}