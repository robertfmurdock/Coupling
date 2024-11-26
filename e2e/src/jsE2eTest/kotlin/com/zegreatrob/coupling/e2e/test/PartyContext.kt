package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon

fun <C1 : PartyContext> C1.attachParty(): suspend (Pair<ActionCannon<CouplingSdkDispatcher>, PartyDetails>) -> C1 = { pair: Pair<ActionCannon<CouplingSdkDispatcher>, PartyDetails> ->
    also {
        this.party = pair.second
        this.sdk = pair.first
    }
}

abstract class PartyContext : SdkContext() {
    lateinit var party: PartyDetails
}
