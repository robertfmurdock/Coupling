package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

val pinRouter = Router(routerParams(mergeParams = true)).apply {
    route("/")
        .post(handleRequest { performSavePinCommand })
    route("/:pinId")
        .delete(handleRequest { performDeletePinCommand })
}
