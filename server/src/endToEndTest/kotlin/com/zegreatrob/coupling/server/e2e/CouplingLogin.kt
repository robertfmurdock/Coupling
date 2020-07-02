package com.zegreatrob.coupling.server.e2e

object CouplingLogin {

    val sdkProvider by lazyDeferred { authorizedSdk("user-${randomInt()}@email.com") }

}