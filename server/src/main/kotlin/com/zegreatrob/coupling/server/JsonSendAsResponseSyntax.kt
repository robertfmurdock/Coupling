package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.Json

interface JsonSendAsResponseSyntax {
    fun Json?.sendAs(response: Response, statusCode: Int = 200) {
        response.statusCode = statusCode
        response.send(this)
    }
}