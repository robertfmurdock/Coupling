package com.zegreatrob.coupling.e2e.test

object CouplingLogin {
    val sdk by lazyDeferred { authorizedKtorCouplingSdk() }
}
