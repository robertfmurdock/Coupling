package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : PartyContext> C1.attachParty(): suspend (Pair<Sdk, Party>) -> C1 = { pair: Pair<Sdk, Party> ->
    also {
        this.party = pair.second
        this.sdk = pair.first
    }
}

abstract class PartyContext : SdkContext() {
    lateinit var party: Party
}
