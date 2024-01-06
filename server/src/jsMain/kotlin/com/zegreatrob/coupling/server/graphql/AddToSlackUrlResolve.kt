package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.slack.slackInstallProvider
import com.zegreatrob.coupling.server.slack.slackRedirectUri
import js.objects.jso
import kotlin.js.Json
import kotlin.js.Promise

val addToSlackUrlResolve: (Json, Json, Request, Json) -> Promise<String> = { _, _, _, _ ->
    slackInstallProvider.generateInstallUrl(
        jso {
            scopes = arrayOf(
                "chat:write",
                "chat:write.customize",
                "channels:history",
                "groups:history",
                "commands",
            )
            redirectUri = slackRedirectUri()
        },
    )
}
