package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.external.express.Router

val tribeRouter = Router(routerParams(mergeParams = true)).apply {
    route("/*").all(handler = { request, response, next ->
        request.commandDispatcher.unsafeCast<CommandDispatcher>()
            .performUserIsAuthorizedAction(request.params["tribeId"].toString())
            .then { isAuthorized ->
                if (isAuthorized) {
                    next()
                } else {
                    response.sendStatus(404)
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
        .get(handleRequest { performTribeListQuery })
        .post(handleRequest { performSaveTribeCommand })
    route("/:tribeId")
        .get(handleRequest { performTribeQuery })
        .post(handleRequest { performSaveTribeCommand })
        .delete(handleRequest { performDeleteTribeCommand })
    use("/:tribeId", tribeRouter)
}
