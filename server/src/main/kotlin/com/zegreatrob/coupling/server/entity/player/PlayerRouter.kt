package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.express.route.routerParams
import com.zegreatrob.coupling.server.external.express.Router

val playerRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
        route("/")
            .post(savePlayerRoute)
        route("/:playerId")
            .delete(deletePlayerRoute)
    }
}
