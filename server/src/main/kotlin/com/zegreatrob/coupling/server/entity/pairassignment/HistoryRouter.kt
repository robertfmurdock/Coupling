package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.external.express.Router
import com.zegreatrob.coupling.server.express.route.routerParams

val historyRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
        route("").post(savePairsRoute)
        route("/:id").delete(deletePairsRoute)
    }
}
