package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import kotools.types.collection.NotEmptyList

data class Round(val pairs: NotEmptyList<CouplingPair>, val gameSpin: GameSpin?)
