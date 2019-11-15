package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

@Suppress("unused")
@JsName("playerRouter")
val playerRouter = Router(
    routerParams(
        mergeParams = true
    )
).apply {
    route("/")
        .get(handleRequest { performPlayersQuery })
        .post(handleRequest { performSavePlayerCommand })
    route("/:playerId")
        .delete(handleRequest { performDeletePlayerCommand })
    route("/retired")
        .get(handleRequest { performRetiredPlayersQuery })
}