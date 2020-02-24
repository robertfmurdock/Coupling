package com.zegreatrob.coupling.server.e2e

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

object CouplingLogin {

    val sdkProvider by lazy {
        GlobalScope.async { authorizedSdk() }
    }

    val login by lazy {
        GlobalScope.async {
            val sdk = sdkProvider.await()
            TestLogin.login(sdk.userEmail)
        }
    }

}