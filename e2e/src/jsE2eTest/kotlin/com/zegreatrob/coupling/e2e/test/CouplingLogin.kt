package com.zegreatrob.coupling.e2e.test

object CouplingLogin {
    val sdk by lazyDeferred { authorizedKtorCouplingSdk() }
}

suspend fun sdk() = CouplingLogin.sdk.await()
