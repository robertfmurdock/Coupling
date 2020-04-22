package com.zegreatrob.coupling.server

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.static
import com.zegreatrob.coupling.server.external.googleauthlibrary.OAuth2Client
import com.zegreatrob.coupling.server.external.passportazuread.OIDCStrategy
import com.zegreatrob.coupling.server.external.passportcustom.Strategy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json


@JsModule("compression")
@JsNonModule
external fun compression(): dynamic

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): dynamic

@JsModule("express-session")
@JsNonModule
external fun session(config: Json): dynamic

@JsModule("express-session")
@JsNonModule
external val expressSession: dynamic

@JsModule("connect-dynamodb")
@JsNonModule
external fun connectDynamoDb(session: dynamic)

fun newDynamoDbStore(@Suppress("UNUSED_PARAMETER") config: Json): dynamic {
    @Suppress("UNUSED_VARIABLE") val store = connectDynamoDb(expressSession)
    return js("new store(config)")
}

@JsModule("serve-favicon")
@JsNonModule
external fun favicon(iconPath: String): dynamic

@JsModule("on-finished")
@JsNonModule
external fun onFinished(response: Response, callback: dynamic)

@JsModule("method-override")
@JsNonModule
external fun methodOverride()

@JsModule("cookie-parser")
@JsNonModule
external fun cookieParser()

@JsModule("errorhandler")
@JsNonModule
external fun errorHandler()

@JsModule("passport")
@JsNonModule
external val passport: dynamic

fun configureExpress(app: Express) {
    app.use(compression())
    app.use(statsd(json("host" to "statsd", "port" to 8125)))
    app.set("port", Config.port)
    app.set("views", arrayOf(resourcePath("public"), resourcePath("views")))
    app.set("view engine", "pug")
    app.use(favicon(resourcePath("public/images/favicon.ico")))

    if (Process.getEnv("DISABLE_LOGGING") == null) {
        app.use(logRequests())
    }

    app.use(urlencoded(json("extended" to true)))
    app.use(com.zegreatrob.coupling.server.external.bodyparser.json())
    app.use(methodOverride())

    app.use(static(resourcePath("public"), json("extensions" to arrayOf("json"))))
    app.use(cookieParser())
    app.use(buildSessionHandler())
    app.use(passport.initialize())
    app.use(passport.session())

    app.use(logoutOnError())

    val isInDevelopmentMode = when (app.get("env")) {
        "development" -> true
        "test" -> true
        else -> false
    }

    if (isInDevelopmentMode) {
        app.use(errorHandler())
    }

    initializeLogging(isInDevelopmentMode)

    passport.serializeUser(UserDataService::serializeUser)
    passport.deserializeUser(UserDataService::deserializeUser)

    passport.use(googleAuthTransferStrategy())
    passport.use(azureODICStrategy())

    if (isInDevelopmentMode) {
        passport.use(com.zegreatrob.coupling.server.external.passportlocal.Strategy(fun(
            username: String,
            password: String,
            done: (dynamic, dynamic) -> Unit
        ) {
            GlobalScope.promise {
                UserDataService.findOrCreate("$username._temp", uuid4())
            }.then({ done(null, it) },
                { done(it, null) })
        }))
    }
}

fun azureODICStrategy(): dynamic {
    return OIDCStrategy(
        json(
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
            "passReqToCallback" to Config.microsoft.passReqToCallback,
            "scope" to Config.microsoft.scope,
            "loggingLevel" to Config.microsoft.loggingLevel,
            "nonceLifetime" to Config.microsoft.nonceLifetime,
            "nonceMaxAmount" to Config.microsoft.nonceMaxAmount,
            "useCookieInsteadOfSession" to Config.microsoft.useCookieInsteadOfSession,
            "cookieEncryptionKeys" to Config.microsoft.cookieEncryptionKeys,
            "clockSkew" to Config.microsoft.clockSkew
        ),
        fun(
            iss: String,
            sub: String,
            profile: Json,
            accessToken: String,
            refreshToken: String,
            done: (dynamic, dynamic) -> Unit
        ) {
            GlobalScope.promise {
                val email = profile["_json"].unsafeCast<Json>()["email"].unsafeCast<String?>()
                email?.let {
                    UserDataService.findOrCreate(email, uuid4())
                }
            }.then({ if (it != null) done(null, it) else done("Auth succeeded but no email found", null) },
                { done(it, null) })
        }
    )
}

fun googleAuthTransferStrategy(): dynamic {
    val clientID = Config.googleClientID
    val client = OAuth2Client(clientID)

    return Strategy { request, done ->
        GlobalScope.promise {
            val payload = client.verifyIdToken(
                json("idToken" to request.body.idToken, "audience" to clientID)
            ).await().getPayload()

            UserDataService.findOrCreate(payload.email, request.traceId ?: uuid4())
        }.then({ done(null, it) }, { done(it, null) })
    }
}

private fun logoutOnError() = { err: dynamic, request: Request, _: Response, next: (dynamic) -> Unit ->
    val error = err.unsafeCast<Any?>()
    next(error?.also { request.logout() })
}

fun buildSessionHandler(): dynamic {
    return session(
        json(
            "secret" to Config.secret,
            "resave" to true,
            "saveUninitialized" to true,
            "store" to sessionStore()
        )
    )
}

fun sessionStore() = newDynamoDbStore(json("client" to DynamoDbProvider.dynamoDB))

private fun logRequests() = { request: Request, response: Response, next: () -> Unit ->
    logRequestAsync(request, response) { callback -> onFinished(response, callback) }
        .also { next() }
}

fun resourcePath(directory: String) = "$directory"
