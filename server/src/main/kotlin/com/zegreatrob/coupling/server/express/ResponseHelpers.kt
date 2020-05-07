package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.action.NotFoundResult
import com.zegreatrob.coupling.server.action.Result
import com.zegreatrob.coupling.server.action.SuccessfulResult
import com.zegreatrob.coupling.server.action.UnauthorizedResult
import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.json

object ResponseHelpers : JsonSendToResponseSyntax {

    fun <V> response(response: Response, result: Result<V>, toJson: (V) -> Any?) = when (result) {
        is SuccessfulResult -> response.sendSuccessful(toJson(result.value))
        is NotFoundResult -> json("message" to "${result.entityName} could not be deleted.").sendTo(response, 404)
        is UnauthorizedResult -> response.sendStatus(403)
    }

}

fun Response.sendSuccessful(body: Any?) {
    this.statusCode = 200
    send(body)
}
