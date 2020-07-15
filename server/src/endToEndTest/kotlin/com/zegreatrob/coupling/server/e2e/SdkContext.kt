package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.sdk.Sdk

abstract class SdkContext {
    lateinit var sdk: Sdk
}

fun <C : SdkContext> C.attach(sdk: Sdk) = also {
    this.sdk = sdk
}

fun <C : SdkContext> sdkSetup(context: C, additionalActions: suspend C.() -> Unit) = e2eSetup(
    { context.attachSdk() }, additionalActions = additionalActions
)

private suspend fun <C : SdkContext> C.attachSdk() = attach(CouplingLogin.sdkProvider.await())
