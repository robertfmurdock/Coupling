package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : TribeContext> C1.attachTribe(tribe: Tribe, sdk: Sdk) = also {
    this.tribe = tribe
    this.sdk = sdk
}

fun <C1 : TribeContext> C1.attachTribe(): suspend (Pair<Sdk, Tribe>) -> C1 = { pair: Pair<Sdk, Tribe> ->
    also {
        this.tribe = pair.second
        this.sdk = pair.first
    }
}

abstract class TribeContext : SdkContext() {
    lateinit var tribe: Tribe
}
