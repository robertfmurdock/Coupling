package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.sdk.axios.axios.axios
import kotlinx.coroutines.await

interface SdkLogout {
    suspend fun logout() = axios.get("/api/logout").await().unsafeCast<Unit>()
}