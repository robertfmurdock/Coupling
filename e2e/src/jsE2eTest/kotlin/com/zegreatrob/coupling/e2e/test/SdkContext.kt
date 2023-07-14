package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon

abstract class SdkContext {
    lateinit var sdk: ActionCannon<CouplingSdkDispatcher>
}

fun <C : SdkContext> C.attach(sdk: ActionCannon<CouplingSdkDispatcher>) = also {
    this.sdk = sdk
}

fun <C : SdkContext> sdkSetup(context: C, additionalActions: suspend C.() -> Unit) = e2eSetup.with(
    { context.attachSdk() },
    additionalActions = additionalActions,
)

private suspend fun <C : SdkContext> C.attachSdk() = attach(CouplingLogin.sdk.await())
