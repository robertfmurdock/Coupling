package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router
import kotlinx.coroutines.launch

val tribeRouter = Router(routerParams(mergeParams = true)).apply {
    route("/*").all(handler = { request, response, next ->
        with(request.commandDispatcher()) {
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

    route("/spin").post(handleRequest { performProposeNewPairsCommand })
    use("/history", historyRouter)
    use("/players", playerRouter)
    use("/pins", pinRouter)
}

@Suppress("unused")
@JsName("tribeListRouter")
val tribeListRouter = Router(routerParams()).apply {
    route("/")
        .post(handleRequest { performSaveTribeCommand })
    route("/:tribeId")
        .post(handleRequest { performSaveTribeCommand })
        .delete(handleRequest { performDeleteTribeCommand })
    use("/:tribeId", tribeRouter)
}
