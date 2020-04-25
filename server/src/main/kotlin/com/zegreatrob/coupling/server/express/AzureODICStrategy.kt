package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.passportazuread.OIDCStrategy
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

fun azureODICStrategy() = OIDCStrategy(
    azureOidcConfig(),
    fun(request, _, _, profile, _, _, done) {
        MainScope().promise {
            val email = profile["_json"].unsafeCast<Json>()["email"].unsafeCast<String?>()
            email?.let {
                UserDataService.findOrCreate(email, request.traceId)
            }
        }.then({ if (it != null) done(null, it) else done("Auth succeeded but no email found", null) },
            { done(it, null) })
    })

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