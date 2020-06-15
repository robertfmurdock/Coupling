package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : TribeContext> C1.attachTribe(tribe: Tribe, sdk: Sdk) = also {
    this.tribe = tribe
    this.sdk = sdk
}

abstract class TribeContext : SdkContext() {
    lateinit var tribe: Tribe
}
