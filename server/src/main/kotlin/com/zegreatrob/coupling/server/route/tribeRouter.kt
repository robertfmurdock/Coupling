package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

@Suppress("unused")
@JsName("tribeListRouter")
val tribeListRouter = Router(routerParams()).apply {
    route("/")
        .get(handleRequest { performTribeListQuery })
        .post(handleRequest { performSaveTribeCommand })
    route("/:tribeId")
        .get(handleRequest { performTribeQuery })
        .post(handleRequest { performSaveTribeCommand })
        .delete(handleRequest { performDeleteTribeCommand })
}
