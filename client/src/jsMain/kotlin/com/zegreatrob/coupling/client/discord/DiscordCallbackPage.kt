package com.zegreatrob.coupling.client.discord

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.slack.InstallPageFrame
import com.zegreatrob.minreact.nfc
import react.dom.html.ReactHTML.div
import react.router.dom.useSearchParams

val DiscordCallbackPage by nfc<PageProps> { _ ->
    val (urlSearchParams) = useSearchParams()
    val code = urlSearchParams["code"]
    val state = urlSearchParams["state"]
    val guildId = urlSearchParams["guild_id"]
    InstallPageFrame {
        title = "Discord Install"
        if (code == null || state == null || guildId == null) {
            +"code, state or guild missing"
        } else {
            div { +"code is $code" }
            div { +"state is $state" }
            div { +"guild_id is $guildId" }
        }
    }
}
