package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router


@Suppress("unused")
@JsName("tribeRouter")
val tribeRouter = Router(routerParams()).apply {
    route("/")
        .get(handleRequest { performTribeListQuery })
        .post(handleRequest { performSaveTribeCommand })
    route("/:tribeId")
        .get(handleRequest { performTribeQuery })
        .post(handleRequest { performSaveTribeCommand })
        .delete(handleRequest { performDeleteTribeCommand })
}
