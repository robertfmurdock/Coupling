package com.zegreatrob.coupling.server.secret.external.jose

@Suppress("NOTHING_TO_INLINE")
inline operator fun JWTPayload.get(key: String): String? = asDynamic()[key]
