package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlin.js.Json
import kotlin.js.json

fun User.toJson() = json(
    "id" to id,
    "email" to email,
    "authorizedTribeIds" to authorizedTribeIds.map { it.value }.toTypedArray()
)

fun Json.toUser() = User(
    id = this["id"].toString(),
    email = this["email"].toString(),
    authorizedTribeIds = this["authorizedTribeIds"].unsafeCast<Array<String>>()
        .map { tribeIdValue -> TribeId(tribeIdValue) }
        .toSet()
)
