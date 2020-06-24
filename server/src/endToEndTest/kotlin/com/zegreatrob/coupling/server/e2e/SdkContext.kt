package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.testmints.async.asyncSetup

abstract class SdkContext {
    lateinit var sdk: Sdk
}

fun <C : SdkContext> C.attach(sdk: Sdk) = also {
    this.sdk = sdk
}

fun <C : SdkContext> sdkSetup(context: C, additionalActions: suspend C.() -> Unit) = asyncSetup(
    contextProvider = attachSdk(context), additionalActions = additionalActions
)

private fun <C : SdkContext> attachSdk(context: C) = suspend { context.attach(CouplingLogin.sdkProvider.await()) }