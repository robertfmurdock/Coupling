package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.NotEmptyList

data class Game(val players: NotEmptyList<Player>, val history: List<PairingSet>, val rule: PairingRule)
