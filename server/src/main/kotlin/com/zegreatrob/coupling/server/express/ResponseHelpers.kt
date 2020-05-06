package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.json

object ResponseHelpers : JsonSendToResponseSyntax {
    fun sendDeleteResults(entityName: String): Response.(Boolean) -> Unit = { result: Boolean ->
        if (result) {
            json("message" to "SUCCESS")
                .sendTo(this)
        } else {
            json(
                "message" to "$entityName could not be deleted."
            )
                .sendTo(this, 404)
        }
    }

}