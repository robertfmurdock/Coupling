package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.tribe.TribeId

data class Boost(val id: String, val userId: String, val tribeIds: Set<TribeId>)
