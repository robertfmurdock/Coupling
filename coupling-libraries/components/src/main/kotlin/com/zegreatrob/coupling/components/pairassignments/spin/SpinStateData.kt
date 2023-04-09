package com.zegreatrob.coupling.components.pairassignments.spin

import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.player.Player

data class SpinStateData(
    val rosterPlayers: List<Player>,
    val revealedPairs: List<PinnedCouplingPair>,
    val shownPlayer: Player?,
)
