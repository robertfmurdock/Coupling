package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler

fun cliRoute(): Handler = { request, response, _ ->
    val type = request.query["type"]
    response.redirect(
        when (type) {
            "js" -> "${Config.cliUrl}/coupling-cli-js.tgz"
            else -> "${Config.cliUrl}/coupling-cli.tgz"
        },
    )
}
