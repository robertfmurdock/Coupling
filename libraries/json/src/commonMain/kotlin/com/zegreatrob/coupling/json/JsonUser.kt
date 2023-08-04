package com.zegreatrob.coupling.json

import kotlinx.serialization.Serializable

@Serializable
data class JsonUser(
    val id: String,
    val details: JsonUserDetails? = null,
    val boost: JsonBoostRecord? = null,
)
