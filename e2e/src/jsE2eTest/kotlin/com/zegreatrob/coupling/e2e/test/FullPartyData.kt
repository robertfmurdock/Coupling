package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon

data class FullPartyData(
    val players: List<Player>,
    val pins: List<Pin>,
    val party: PartyDetails,
    val sdk: ActionCannon<CouplingSdkDispatcher>,
)
