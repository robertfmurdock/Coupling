package com.zegreatrob.coupling.model.user

import com.zegreatrob.coupling.model.tribe.TribeId

data class User(
    val id: String,
    val email: String,
    val authorizedTribeIds: Set<TribeId>
)