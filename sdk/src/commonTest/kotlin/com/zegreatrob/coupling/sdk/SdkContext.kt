package com.zegreatrob.coupling.sdk

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.async.asyncTestTemplate

interface SdkContext : SdkProviderSyntax {
    override val sdk: ActionCannon<CouplingSdkDispatcher>
    val username: String
}

val asyncSetup = asyncTestTemplate<SdkContext>(beforeAll = {
    val sdk = sdk()
    object : SdkContext {
        override val username = PRIMARY_AUTHORIZED_USER_NAME
        override val sdk = sdk
    }
})
