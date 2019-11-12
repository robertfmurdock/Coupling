package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.user.User
import kotlin.js.json

interface UserJsonSyntax {
    fun User.toJson() = json(
        "email" to email,
        "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )
}
