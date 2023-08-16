package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player

data class PartySetup(val party: PartyDetails, val players: List<Player>, val history: List<PairAssignmentDocument>)

expect fun loadJsonPartySetup(fileResource: String): PartySetup
expect inline fun <reified T> loadResource(fileResource: String): T
