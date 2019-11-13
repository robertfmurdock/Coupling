package com.zegreatrob.coupling.server

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

    fun <T> sendQueryResults(entityName: String): Response.(T?) -> Unit = { result ->
        if (result != null) {
            send(result)
        } else {
            json("message" to "$entityName not found")
                .sendTo(this, 404)
        }
    }

}