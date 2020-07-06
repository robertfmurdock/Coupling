package com.zegreatrob.coupling.server.external.expressws

import com.zegreatrob.coupling.server.express.route.WebSocketServer
import com.zegreatrob.coupling.server.external.express.Express

@JsModule("express-ws")
@JsNonModule
external fun expressWs(express: Express): ExpressWs

external interface ExpressWs {
    fun getWss(): WebSocketServer

    val app: Express
}
