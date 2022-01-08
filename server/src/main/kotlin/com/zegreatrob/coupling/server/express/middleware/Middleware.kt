package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.bodyparser.bodyParserJson
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.compression.compression
import com.zegreatrob.coupling.server.external.cookie_parser.cookieParser
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.method_override.methodOverride
import kotlin.js.json

fun Express.middleware() {
    use(compression())
    use(statsD())
    set("port", Config.port)
    use(tracer())
    use(scope())
    if (!Config.disableLogging) {
        use(logRequests())
    }
    use(urlencoded(json("extended" to true)))
    use(bodyParserJson())
    use(methodOverride())
    use(staticResourcesPublic())
    use(cookieParser())
    initializeLogging(false)
}
