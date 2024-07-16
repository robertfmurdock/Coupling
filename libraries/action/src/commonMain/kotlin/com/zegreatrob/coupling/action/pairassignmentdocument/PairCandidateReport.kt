package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.player.Player

data class PairCandidateReport(val player: Player, val partners: List<Player>, val timeResult: TimeResult)
