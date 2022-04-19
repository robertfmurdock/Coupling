package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.Json

interface JsonSendToResponseSyntax {

    fun Json?.sendTo(response: Response, statusCode: Int = 200) {
        response.statusCode = statusCode
        response.send(this)
    }
}
