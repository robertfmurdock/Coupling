package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router
import com.zegreatrob.coupling.server.player.deletePlayerRoute
import com.zegreatrob.coupling.server.player.retiredPlayerRoute
import com.zegreatrob.coupling.server.player.savePlayerRoute

val playerRouter = Router(routerParams(mergeParams = true)).apply {
    route("/")
        .post(savePlayerRoute)
    route("/:playerId")
        .delete(deletePlayerRoute)
    route("/retired")
        .get(retiredPlayerRoute)
}
