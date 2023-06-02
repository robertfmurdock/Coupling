package com.zegreatrob.coupling.sdk

import com.zegreatrob.testmints.async.asyncTestTemplate

interface SdkContext : SdkProviderSyntax {
    override val sdk: CouplingSdk
    val username: String
}

val asyncSetup = asyncTestTemplate<SdkContext>(beforeAll = {
    val sdk = sdk()
    object : SdkContext {
        override val username = primaryAuthorizedUsername
        override val sdk = sdk
    }
})
