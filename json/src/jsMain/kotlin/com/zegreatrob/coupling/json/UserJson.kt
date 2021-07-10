package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class JsonUser(
    val id: String,
    val email: String,
    val authorizedTribeIds: Set<String>
)

fun User.toSerializable() = JsonUser(
    id = id,
    email = email,
    authorizedTribeIds = authorizedTribeIds.map { it.value }.toSet()
)

fun JsonUser.toModel() = User(
    id = id,
    email = email,
    authorizedTribeIds = authorizedTribeIds.map(::TribeId).toSet(),
)
