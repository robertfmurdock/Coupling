package com.zegreatrob.coupling.server.e2e

object CouplingLogin {

    val sdkProvider by lazyDeferred { authorizedSdk("user-${randomInt()}@email.com") }

    val loginProvider by lazyDeferred {
        val sdk = sdkProvider.await()
        TestLogin.login(sdk.userEmail)
    }

}