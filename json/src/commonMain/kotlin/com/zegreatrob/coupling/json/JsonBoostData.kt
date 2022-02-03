package com.zegreatrob.coupling.json

import kotlinx.serialization.Serializable

@Serializable
data class SaveBoostInput(val tribeIds: List<String>)
