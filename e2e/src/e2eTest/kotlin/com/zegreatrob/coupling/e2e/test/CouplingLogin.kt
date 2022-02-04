package com.zegreatrob.coupling.e2e.test

object CouplingLogin {
    val sdkProvider by lazyDeferred { authorizedKtorSdk() }
}
