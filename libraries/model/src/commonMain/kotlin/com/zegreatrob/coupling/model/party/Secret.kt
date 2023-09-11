package com.zegreatrob.coupling.model.party

import kotlinx.datetime.Instant

data class Secret(val id: String, val description: String, val createdTimestamp: Instant)
