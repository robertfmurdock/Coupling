package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.testmints.async.asyncTestTemplate

interface SdkContext : SdkSyntax {
    override val sdk: AuthorizedKtorSdk
    val username: String
}

fun <T> sdkContext(block: suspend (SdkContext) -> T): suspend (Unit) -> T = {
    val username = "eT-user-${uuid4()}"
    val sdk = authorizedKtorSdk(username = username)
    block(object : SdkContext {
        override val username = username
        override val sdk = sdk
    })
}


val sdkSetup = asyncTestTemplate<SdkContext>(beforeAll = {
    val username = "eT-user-${uuid4()}"
    val sdk = authorizedKtorSdk(username = username)
    object : SdkContext {
        override val username = username
        override val sdk = sdk
    }
})
