package com.zegreatrob.coupling.server

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.external.bodyparser.bodyParserJson
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.compression.compression
import com.zegreatrob.coupling.server.external.connect_dynamodb.newDynamoDbStore
import com.zegreatrob.coupling.server.external.cookie_parser.cookieParser
import com.zegreatrob.coupling.server.external.errorhandler.errorHandler
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.static
import com.zegreatrob.coupling.server.external.express_session.session
import com.zegreatrob.coupling.server.external.googleauthlibrary.OAuth2Client
import com.zegreatrob.coupling.server.external.method_override.methodOverride
import com.zegreatrob.coupling.server.external.on_finished.onFinished
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.external.passportazuread.OIDCStrategy
import com.zegreatrob.coupling.server.external.passportcustom.Strategy
import com.zegreatrob.coupling.server.external.serve_favicon.favicon
import com.zegreatrob.coupling.server.external.statsd.statsd
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json


fun Express.configureExpress() {
    use(compression())
    use(statsd(json("host" to "statsd", "port" to 8125)))
    set("port", Config.port)
    set("views", arrayOf(resourcePath("public"), resourcePath("views")))
    set("view engine", "pug")
    use(favicon(resourcePath("public/images/favicon.ico")))
    use(tracer())
    if (Config.disableLogging) {
        use(logRequests())
    }
    use(urlencoded(json("extended" to true)))
    use(bodyParserJson())
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

private fun Express.configPassport() {
    use(passport.initialize())
    use(passport.session())

    passport.serializeUser(UserDataService::serializeUser)
    passport.deserializeUser(UserDataService::deserializeUser)

    passport.use(googleAuthTransferStrategy())
    passport.use(azureODICStrategy())

    if (isInDevMode()) {
        passport.use(LocalStrategy(json("passReqToCallback" to true)) { request, username, _, done ->
            doneAfter(done, UserDataService.findOrCreate("$username._temp", request.traceId))
        })
    }
}

private fun doneAfter(done: (dynamic, dynamic) -> Unit, promise: Promise<User>) {
    promise.then({ done(null, it) }, { done(it, null) })
}

typealias LocalStrategy = com.zegreatrob.coupling.server.external.passportlocal.Strategy

private fun tracer(): Handler = { request, _, next -> request.asDynamic().traceId = uuid4(); next() }

private fun Express.isInDevMode() = when (get("env")) {
    "development" -> true
    "test" -> true
    else -> false
}

fun azureODICStrategy() = OIDCStrategy(azureOidcConfig(), fun(request, _, _, profile, _, _, done) {
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

fun sessionStore() =
    newDynamoDbStore(json("client" to DynamoDbProvider.dynamoDB))

private fun logRequests(): Handler = { request, response, next ->
    logRequestAsync(request, response) { callback ->
        onFinished(
            response,
            callback
        )
    }
        .also { next() }
}

fun resourcePath(directory: String) = "${js("__dirname")}/../../../server/build/executable/$directory"
