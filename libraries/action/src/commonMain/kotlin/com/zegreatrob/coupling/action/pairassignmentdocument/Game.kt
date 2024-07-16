package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.NotEmptyList

data class Game(val players: NotEmptyList<Player>, val history: List<PairAssignmentDocument>, val rule: PairingRule)
