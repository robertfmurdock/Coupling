package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import kotlin.js.json

interface AxiosCreateGoogleSession {
    fun createSessionOnCoupling(idToken: String) = axios.post(
            "/auth/google-token",
            json("idToken" to idToken)
    )
}
