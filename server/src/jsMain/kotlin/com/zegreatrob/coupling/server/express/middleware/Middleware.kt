package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.jwtMiddleware
import com.zegreatrob.coupling.server.external.compression.compression
import com.zegreatrob.coupling.server.external.cookieparser.cookieParser
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.methodoverride.methodOverride

fun Express.middleware() {
    use(compression())
    use(statsD())
    set("port", Config.port)
    use(tracer())
    use(scope())
    if (!Config.disableLogging) {
        use(logRequests())
    }
    use(methodOverride())
    use(staticResourcesPublic())
    use(cookieParser())
    initializeLogging(false)
    use(jwtMiddleware())
}