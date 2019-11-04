package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.external.axios.axios
import kotlin.js.json

interface SdkCreateGoogleSession {
    fun createSessionOnCoupling(idToken: String) = axios.post(
        "/auth/google-token",
        json("idToken" to idToken)
    )
}
