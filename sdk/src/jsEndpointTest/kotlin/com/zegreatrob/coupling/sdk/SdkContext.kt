package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4

interface SdkContext : SdkSyntax {
    override val sdk: AuthorizedSdk
    val username: String
}

fun <T> sdkContext(block: suspend (SdkContext) -> T): suspend (Unit) -> T = {
    val username = "eT-user-${uuid4()}"
    val sdk = authorizedSdk(username = username)
    block(object : SdkContext {
        override val username = username
        override val sdk = sdk
    })
}
