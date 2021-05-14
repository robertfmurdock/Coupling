package com.zegreatrob.coupling.e2e.test.external.webdriverio

object CouplingLogin {
    val sdkProvider by lazyDeferred { authorizedSdk("user-${randomInt()}@email.com") }
}
