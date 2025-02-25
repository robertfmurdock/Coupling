package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import react.createContext

val pairContext = createContext<Set<CouplingPair>>(emptySet())
