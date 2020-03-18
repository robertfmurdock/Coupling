package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.User
import kotlin.js.json

fun User.toJson() = json(
    "id" to id,
    "email" to email,
    "authorizedTribeIds" to authorizedTribeIds.map { it.value }.toTypedArray()
)
