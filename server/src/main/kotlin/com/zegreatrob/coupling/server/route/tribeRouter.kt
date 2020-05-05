package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router
import com.zegreatrob.coupling.server.pairassignments.historyRouter
import com.zegreatrob.coupling.server.pin.pinRouter
import com.zegreatrob.coupling.server.player.playerRouter
import kotlinx.coroutines.launch

val tribeRouter = Router(routerParams(mergeParams = true)).apply {
    route("/*").all(handler = { request, response, next ->
        with(request.commandDispatcher) {
            scope.launch {
                val isAuthorized = performUserIsAuthorizedAction(request.params["tribeId"].toString())
                if (isAuthorized) {
                    next()
                } else {
                    response.sendStatus(404)
                }
            }
        }
    })

    route("/spin").post(dispatch { performProposeNewPairsCommand })
    use("/history", historyRouter)
    use("/players", playerRouter)
    use("/pins", pinRouter)
}

val tribeListRouter = Router(routerParams()).apply {
    route("/")
        .post(dispatch { performSaveTribeCommand })
    route("/:tribeId")
        .post(dispatch { performSaveTribeCommand })
        .delete(dispatch { performDeleteTribeCommand })
    use("/:tribeId", tribeRouter)
}
