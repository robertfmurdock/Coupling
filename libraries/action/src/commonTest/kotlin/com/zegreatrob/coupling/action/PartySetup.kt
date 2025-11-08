package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player

data class PartySetup(val party: PartyDetails, val players: List<Player>, val history: List<PairingSet>)

expect fun loadJsonPartySetup(fileResource: String): PartySetup
expect inline fun <reified T> loadResource(fileResource: String): T
