package com.zegreatrob.coupling.sdk

import kotlin.js.json

interface SdkCreateGoogleSession : AxiosSyntax {
    fun createSessionOnCouplingAsync(idToken: String) = axios.postAsync<Unit>(
        "/auth/google-token",
        json("idToken" to idToken)
    )
}
