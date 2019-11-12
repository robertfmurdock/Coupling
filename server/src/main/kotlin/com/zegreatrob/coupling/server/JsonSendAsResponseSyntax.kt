package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.Json

interface JsonSendAsResponseSyntax {
    fun Json?.sendTo(response: Response, statusCode: Int = 200) {
        response.statusCode = statusCode
        response.send(this)
    }

    fun Array<*>.sendTo(response: Response, statusCode: Int = 200) {
        response.statusCode = statusCode
        response.send(this)
    }
}