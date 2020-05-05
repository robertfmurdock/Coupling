package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.spinRoute
import com.zegreatrob.coupling.server.entity.tribe.deleteTribeRoute
import com.zegreatrob.coupling.server.entity.tribe.saveTribeRoute
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.pairassignments.historyRouter
import com.zegreatrob.coupling.server.pin.pinRouter
import com.zegreatrob.coupling.server.player.playerRouter
import kotlinx.coroutines.launch


val tribeRouter = Router(routerParams(mergeParams = true)).apply {
    route("/*").all(::authCheck)
    route("/spin").post(spinRoute)
    use("/history", historyRouter)
    use("/players", playerRouter)
    use("/pins", pinRouter)
}

private fun authCheck(request: Request, response: Response, next: Next): Unit = with(request.commandDispatcher) {
    request.scope.launch {
        if (isUserAuthorized(request.tribeId())) {
            next()
        } else {
            response.sendStatus(404)
        }
    }
}

private suspend fun UserIsAuthorizedActionDispatcher.isUserAuthorized(tribeId: TribeId) =
    UserIsAuthorizedAction(tribeId)
        .perform()

val tribeListRouter by lazy {
    Router(routerParams()).apply {
        route("/").post(saveTribeRoute)
        route("/:tribeId")
            .post(saveTribeRoute)
            .delete(deleteTribeRoute)
        use("/:tribeId", tribeRouter)
    }
}