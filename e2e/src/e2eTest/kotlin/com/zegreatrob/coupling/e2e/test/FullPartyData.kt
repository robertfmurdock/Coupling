package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.KtorCouplingSdk

data class FullPartyData(val players: List<Player>, val pins: List<Pin>, val party: PartyDetails, val sdk: KtorCouplingSdk)
