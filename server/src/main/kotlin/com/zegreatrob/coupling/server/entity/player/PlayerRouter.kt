package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.external.express.Router
import com.zegreatrob.coupling.server.route.routerParams

val playerRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
        route("/")
            .post(savePlayerRoute)
        route("/:playerId")
            .delete(deletePlayerRoute)
        route("/retired")
            .get(retiredPlayerRoute)
    }
}
