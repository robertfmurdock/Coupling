package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.UserDataService.findOrCreateUser
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.passportazuread.OIDCStrategy
import kotlin.js.Json
import kotlin.js.json

fun azureODICStrategy() = OIDCStrategy(azureOidcConfig()) { request, _, _, profile, _, _, done ->
    request.scope.async(done,
        findOrCreateUser(profile, request)
    )
}

private fun findOrCreateUser(profile: Json, request: Request) = suspend {
    profile.email()?.let {
        findOrCreateUser(it, request.traceId)
    } ?: throw Exception("Auth succeeded but no email found")
}

private fun Json.email() = this["_json"].unsafeCast<Json>()["email"].unsafeCast<String?>()

private fun azureOidcConfig() = json(
    "identityMetadata" to Config.microsoft.identityMetadata,
    "clientID" to Config.microsoft.clientID,
    "responseType" to Config.microsoft.responseType,
    "responseMode" to Config.microsoft.responseMode,
    "redirectUrl" to Config.microsoft.redirectUrl,
    "allowHttpForRedirectUrl" to Config.microsoft.allowHttpForRedirectUrl,
    "clientSecret" to Config.microsoft.clientSecret,
    "validateIssuer" to Config.microsoft.validateIssuer,
    "isB2C" to Config.microsoft.isB2C,
    "issuer" to Config.microsoft.issuer,
    "passReqToCallback" to true,
    "scope" to Config.microsoft.scope,
    "loggingLevel" to Config.microsoft.loggingLevel,
    "nonceLifetime" to Config.microsoft.nonceLifetime,
    "nonceMaxAmount" to Config.microsoft.nonceMaxAmount,
    "useCookieInsteadOfSession" to Config.microsoft.useCookieInsteadOfSession,
    "cookieEncryptionKeys" to Config.microsoft.cookieEncryptionKeys,
    "clockSkew" to Config.microsoft.clockSkew
)