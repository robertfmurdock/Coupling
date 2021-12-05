package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.external.axios.axios
import kotlinx.coroutines.await

interface SdkLogout {
    suspend fun logout() = axios.get("/api/logout").await().unsafeCast<Unit>()
}