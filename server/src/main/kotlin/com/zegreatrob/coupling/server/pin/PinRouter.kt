package com.zegreatrob.coupling.server.pin

import com.zegreatrob.coupling.server.external.express.Router
import com.zegreatrob.coupling.server.route.routerParams

val pinRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
        route("/").post(savePinRoute)
        route("/:pinId").delete(deletePinRoute)
    }
}
