package com.zegreatrob.coupling.client.discord

import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.slack.InstallPageFrame
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoader
import io.ktor.http.parseUrlEncodedParameters
import js.lazy.Lazy
import react.Fragment
import react.create
import react.dom.html.ReactHTML.div
import tanstack.react.router.useSearch

@Lazy
val DiscordCallbackPage by nfc<PageProps> { props ->
    val search = useSearch()
    val code = search["code"]?.toString()
    val state = search["state"]?.toString()

    val stateData = state?.parseUrlEncodedParameters()
    val partyId = stateData?.get("partyId")
    val guildId = search["guild_id"]?.toString()
    InstallPageFrame {
        title = "Discord Install"
        if (code == null || state == null || guildId == null || partyId == null) {
            +"code, state, guild, or party id missing"
        } else {

            DataLoader(
                getDataAsync = {
                    props.commander.tracingCannon()
                        .fire(GrantDiscordAccessCommand(code, guildId, PartyId(partyId)))
                },
                errorData = { VoidResult.Rejected },
                child = {
                    Fragment.create {
                        div { +"code is $code" }
                        div { +"state is $state" }
                        div { +"guild_id is $guildId" }
                    }
                },
            )
        }
    }
}
