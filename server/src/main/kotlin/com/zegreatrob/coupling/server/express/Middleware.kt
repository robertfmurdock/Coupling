package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.server.external.bodyparser.bodyParserJson
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.compression.compression
import com.zegreatrob.coupling.server.external.cookie_parser.cookieParser
import com.zegreatrob.coupling.server.external.errorhandler.errorHandler
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.method_override.methodOverride
import com.zegreatrob.coupling.server.external.serve_favicon.favicon
import kotlin.js.json

fun Express.middleware() {
    use(compression())
    use(statsD())
    set("port", Config.port)
    set("views", viewResources())
    set("view engine", "pug")
    use(favicon())
    use(tracer())
    use(scope())
    if (Config.disableLogging) {
        use(logRequests())
    }
    use(urlencoded(json("extended" to true)))
    use(bodyParserJson())
    use(methodOverride())
    use(staticResources())
    use(cookieParser())
    use(session())
    if (isInDevMode) {
        use(errorHandler())
    }
    initializeLogging(isInDevMode)
    passport()
}

private fun favicon() = favicon(resourcePath("public/images/favicon.ico"))

private fun viewResources() = arrayOf(resourcePath("public"), resourcePath("views"))

fun resourcePath(directory: String) = "${js("__dirname")}/../../../server/build/executable/$directory"
