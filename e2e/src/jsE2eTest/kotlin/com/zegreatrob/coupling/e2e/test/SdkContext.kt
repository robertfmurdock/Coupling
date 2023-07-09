package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.sdk.KtorCouplingSdk

abstract class SdkContext {
    lateinit var sdk: KtorCouplingSdk
}

fun <C : SdkContext> C.attach(sdk: KtorCouplingSdk) = also {
    this.sdk = sdk
}

fun <C : SdkContext> sdkSetup(context: C, additionalActions: suspend C.() -> Unit) = e2eSetup.with(
    { context.attachSdk() },
    additionalActions = additionalActions,
)

private suspend fun <C : SdkContext> C.attachSdk() = attach(CouplingLogin.sdk.await())
