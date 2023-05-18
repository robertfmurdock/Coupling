package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.KtorCouplingSdk

fun <C1 : PartyContext> C1.attachParty(): suspend (Pair<KtorCouplingSdk, Party>) -> C1 = { pair: Pair<KtorCouplingSdk, Party> ->
    also {
        this.party = pair.second
        this.sdk = pair.first
    }
}

abstract class PartyContext : SdkContext() {
    lateinit var party: Party
}
