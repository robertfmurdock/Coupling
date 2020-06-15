package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.sdk.Sdk

abstract class SdkContext {
    lateinit var sdk: Sdk
}

fun <C: SdkContext> C.attach(sdk: Sdk) = also {
    this.sdk = sdk
}