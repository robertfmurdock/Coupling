package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import react.createContext

val pairContext = createContext<Set<CouplingPair>>(emptySet())
