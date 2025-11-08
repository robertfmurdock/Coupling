package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.PlayerId
import kotools.types.collection.NotEmptyList
import kotlin.time.Instant

data class PairAssignment(
    val documentId: PairingSetId,
    val playerIds: List<PlayerId>,
    val pairingSet: PartyRecord<PairingSet>? = null,
    val date: Instant,
    val allPairs: NotEmptyList<PinnedCouplingPair>? = null,
    val recentTimesPaired: Int? = null,
)
