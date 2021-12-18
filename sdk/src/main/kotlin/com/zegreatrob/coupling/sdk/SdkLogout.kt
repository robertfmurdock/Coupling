package com.zegreatrob.coupling.sdk

interface SdkLogout : GqlSyntax {
    suspend fun logout() = performer.get("/api/logout").unsafeCast<Unit>()
}
