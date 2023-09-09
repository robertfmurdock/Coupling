package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler

fun cliRoute(): Handler = { _, response, _ ->
    response.redirect("${Config.cliUrl}/coupling-cli.tgz")
}
