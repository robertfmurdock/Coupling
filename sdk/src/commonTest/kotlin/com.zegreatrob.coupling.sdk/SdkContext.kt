package com.zegreatrob.coupling.sdk

import com.zegreatrob.testmints.async.asyncTestTemplate

interface SdkContext : SdkSyntax {
    override val sdk: Sdk
    val username: String
}

val sdkSetup = asyncTestTemplate<SdkContext>(beforeAll = {
    val sdk = authorizedKtorSdk()
    object : SdkContext {
        override val username = primaryAuthorizedUsername
        override val sdk = sdk
    }
})
