package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.entity.tribe.deleteTribeRoute
import com.zegreatrob.coupling.server.entity.tribe.saveTribeRoute
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

    route("/spin").post(dispatchCommand { performProposeNewPairsCommand })
    use("/history", historyRouter)
    use("/players", playerRouter)
    use("/pins", pinRouter)
}

val tribeListRouter by lazy {
    Router(routerParams()).apply {
        route("/").post(saveTribeRoute)
        route("/:tribeId")
            .post(saveTribeRoute)
            .delete(deleteTribeRoute)
        use("/:tribeId", tribeRouter)
    }
}