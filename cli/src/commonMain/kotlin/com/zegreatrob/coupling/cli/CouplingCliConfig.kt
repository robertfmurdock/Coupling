package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.json.PartyIdString
import kotlinx.serialization.Serializable

@Serializable
data class CouplingCliConfig(
    val partyId: PartyIdString? = null,
)
