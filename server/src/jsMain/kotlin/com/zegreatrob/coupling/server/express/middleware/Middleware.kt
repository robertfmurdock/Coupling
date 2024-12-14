package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.jwtMiddleware
import com.zegreatrob.coupling.server.external.cookieparser.cookieParser
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.methodoverride.methodOverride

fun Express.middleware() {
    set("port", Config.port)
    use(tracer())
    use(scope())
    if (!Config.disableLogging) {
        use(logRequests())
    }
    use(methodOverride())
    use(staticResourcesPublic())
    use(cookieParser())
    use(jwtMiddleware())
}
