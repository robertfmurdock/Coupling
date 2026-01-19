package com.zegreatrob.coupling.cli

import com.auth0.jwt.JWT

actual fun decodeJwt(accessToken: String): Map<String, String> = JWT.decode(accessToken).run {
    claims
        .map { it.key to it.value.asString() }
        .toMap()
        .plus("exp" to expiresAtAsInstant.epochSecond.toString())
        .plus("iat" to issuedAtAsInstant.epochSecond.toString())
}
