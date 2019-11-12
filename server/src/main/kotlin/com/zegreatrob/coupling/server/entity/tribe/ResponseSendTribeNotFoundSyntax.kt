package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.JsonSendAsResponseSyntax
import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.json

interface ResponseSendTribeNotFoundSyntax : JsonSendAsResponseSyntax {
    fun Response.sendTribeNotFound() {
        json("message" to "tribe not found")
            .sendTo(this, 404)
    }
}
