package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

val playerRouter = Router(routerParams(mergeParams = true)).apply {
    route("/")
        .post(handleRequest { performSavePlayerCommand })
    route("/:playerId")
        .delete(handleRequest { performDeletePlayerCommand })
    route("/retired")
        .get(handleRequest { performRetiredPlayersQuery })
}