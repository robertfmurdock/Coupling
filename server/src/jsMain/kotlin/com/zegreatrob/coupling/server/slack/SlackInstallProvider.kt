package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.slack.external.oauth.InstallProvider
import js.objects.jso

val slackInstallProvider by lazy {
    InstallProvider(
        jso {
            clientId = Config.slackClientId
            clientSecret = Config.slackClientSecret
            stateSecret = Config.secretSigningSecret
            installUrlOptions = jso {
                scopes = arrayOf("chat:write", "chat:write.customize", "channels:history", "groups:history", "commands")
                redirectUri = slackRedirectUri()
            }
        },
    )
}

val slackRequestVerifier by lazy { SlackRequestVerifier(Config.slackSigningSecret) }
