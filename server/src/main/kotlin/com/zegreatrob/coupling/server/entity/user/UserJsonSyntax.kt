package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlin.js.Json
import kotlin.js.json

interface UserJsonSyntax {
    fun User.toJson() = json(
        "id" to id,
        "email" to email,
        "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )

}

fun Json.toUser() = User(
    id = this["id"].toString(),
    email = this["email"].toString(),
    authorizedTribeIds = this["tribes"].unsafeCast<Array<String>>()
        .map { tribeIdValue -> TribeId(tribeIdValue) }
        .toSet()
)

