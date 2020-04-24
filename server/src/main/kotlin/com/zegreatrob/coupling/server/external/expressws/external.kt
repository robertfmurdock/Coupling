package com.zegreatrob.coupling.server.external.expressws

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.route.WebSocketServer

@JsModule("express-ws")
@JsNonModule
external fun expressWs(express: Express): ExpressWs

external interface ExpressWs {
    fun getWss(): WebSocketServer

    val app: Express
}
