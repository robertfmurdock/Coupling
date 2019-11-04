package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.sdk.axios.axios.axios
import kotlin.js.json

interface SdkCreateGoogleSession {
    fun createSessionOnCoupling(idToken: String) = axios.post(
        "/auth/google-token",
        json("idToken" to idToken)
    )
}
