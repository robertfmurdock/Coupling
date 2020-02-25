package com.zegreatrob.coupling.server.e2e

object CouplingLogin {

    val sdkProvider by lazyDeferred { authorizedSdk() }

    val login by lazyDeferred {
        val sdk = sdkProvider.await()
        TestLogin.login(sdk.userEmail)
    }

}