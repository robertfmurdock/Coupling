package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.jwtMiddleware
import com.zegreatrob.coupling.server.external.express.Express

fun Express.middleware() {
    set("port", Config.port)
    use(tracer())
    use(scope())
    if (!Config.disableLogging) {
        use(logRequests())
    }
    use(staticResourcesPublic())
    use(jwtMiddleware())
}
