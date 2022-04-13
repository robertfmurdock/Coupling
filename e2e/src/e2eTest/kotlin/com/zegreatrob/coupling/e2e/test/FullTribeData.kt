package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.Sdk

data class FullTribeData(val players: List<Player>, val pins: List<Pin>, val tribe: Party, val sdk: Sdk)
