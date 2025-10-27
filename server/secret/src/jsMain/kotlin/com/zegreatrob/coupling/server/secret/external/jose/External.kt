@file:JsModule("jose")

package com.zegreatrob.coupling.server.secret.external.jose

import js.typedarrays.Uint8Array
import kotlinx.js.JsPlainObject
import kotlin.js.Date
import kotlin.js.Promise

external class SignJWT(jwt: dynamic) {

    fun setProtectedHeader(header: dynamic): SignJWT
    fun setIssuedAt(header: dynamic = definedExternally): SignJWT
    fun setIssuer(issuer: String): SignJWT
    fun setAudience(audience: String): SignJWT
    fun setSubject(audience: String): SignJWT
    fun setExpirationTime(exp: String): SignJWT
    fun sign(secret: Uint8Array<*>): Promise<String>
}

external fun jwtVerify(
    token: String,
    secret: Uint8Array<*>,
    options: JWTVerifyOptions = definedExternally,
): Promise<JWTVerifyResult>

@JsPlainObject
sealed external interface JWTVerifyResult {
    val payload: JWTPayload
}

@JsPlainObject
sealed external interface JWTPayload {
    val sub: String
}

@JsPlainObject
sealed external interface JWTVerifyOptions {
    var algorithms: Array<String>?
    var audience: Array<String>?
    var clockTolerance: Int?
    var crit: dynamic
    var currentDate: Date?
    var issuer: Array<String>?
    var maxTokenAge: Int?
    var requiredClaims: Array<String>?
    var subject: String?
    var typ: String?
}
