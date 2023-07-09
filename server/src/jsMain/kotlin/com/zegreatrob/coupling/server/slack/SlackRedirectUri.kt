package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.server.express.Config

fun slackRedirectUri() = "${Config.publicUrl}${Config.clientBasename}/integration/slack/callback"
