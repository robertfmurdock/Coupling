package com.zegreatrob.coupling.server

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.external.googleauthlibrary.OAuth2Client
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.external.passportazuread.OIDCStrategy
import com.zegreatrob.coupling.server.external.passportcustom.Strategy
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json


@JsModule("compression")
@JsNonModule
external fun compression(): Handler

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): Handler

@JsModule("express-session")
@JsNonModule
external fun session(config: Json): Handler

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
external fun favicon(iconPath: String): Handler

@JsModule("on-finished")
@JsNonModule
external fun onFinished(response: Response, callback: dynamic)

@JsModule("method-override")
@JsNonModule
external fun methodOverride(): Handler

@JsModule("cookie-parser")
@JsNonModule
external fun cookieParser(): Handler

@JsModule("errorhandler")
@JsNonModule
external fun errorHandler(): Handler

fun configureExpress(app: Express) {
    configureExpressKt(app)
}

private fun Express.configPassport() {
    use(passport.initialize())
    use(passport.session())

    passport.serializeUser(UserDataService::serializeUser)
    passport.deserializeUser(UserDataService::deserializeUser)

    passport.use(googleAuthTransferStrategy())
    passport.use(azureODICStrategy())

    if (isInDevMode()) {
        passport.use(LocalStrategy { username, _, done ->
            doneAfter(done, UserDataService.findOrCreate("$username._temp", uuid4()))
        })
    }
}

private fun doneAfter(done: (dynamic, dynamic) -> Unit, promise: Promise<User>) {
    promise.then({ done(null, it) }, { done(it, null) })
}

typealias LocalStrategy = com.zegreatrob.coupling.server.external.passportlocal.Strategy

fun configureExpressKt(app: Express) = with(app) {
    configure()
}

private fun Express.configure() {
    use(compression())
    use(statsd(json("host" to "statsd", "port" to 8125)))
    set("port", Config.port)

    set("views", arrayOf(resourcePath("public"), resourcePath("views")))
    set("view engine", "pug")
    use(favicon(resourcePath("public/images/favicon.ico")))
    use(addTraceId())

    if (Config.disableLogging) {
        use(logRequests())
    }

    use(urlencoded(json("extended" to true)))
    use(com.zegreatrob.coupling.server.external.bodyparser.json())
    use(methodOverride())

    use(static(resourcePath("public"), json("extensions" to arrayOf("json"))))
    use(cookieParser())
    use(buildSessionHandler())

    if (isInDevMode()) {
        use(errorHandler())
    }

    initializeLogging(isInDevMode())

    configPassport()
}

private fun addTraceId(): Handler = { request, _, next -> request.asDynamic().traceId = uuid4(); next() }

private fun Express.isInDevMode() = when (get("env")) {
    "development" -> true
    "test" -> true
    else -> false
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
            MainScope().promise {
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
        MainScope().promise {
            val payload = client.verifyIdToken(
                json("idToken" to request.body.idToken, "audience" to clientID)
            ).await().getPayload()

            UserDataService.findOrCreate(payload.email, request.traceId)
        }.then({ done(null, it) }, { done(it, null) })
    }
}

fun buildSessionHandler() = session(
    json(
        "secret" to Config.secret,
        "resave" to true,
        "saveUninitialized" to true,
        "store" to sessionStore()
    )
)

fun sessionStore() = newDynamoDbStore(json("client" to DynamoDbProvider.dynamoDB))

private fun logRequests(): Handler = { request: Request, response: Response, next: () -> Unit ->
    logRequestAsync(request, response) { callback -> onFinished(response, callback) }
        .also { next() }
}

fun resourcePath(directory: String) = "${js("__dirname")}/../../../server/build/executable/$directory"
