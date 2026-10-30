package com.zegreatrob.coupling.client.discord

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.slack.InstallPageFrame
import com.zegreatrob.minreact.nfc
import js.array.component1
import js.lazy.Lazy
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.router.dom.useSearchParams
import react.useEffectOnce
import react.useState
import kotlin.uuid.Uuid

@Lazy
val DiscordCallbackPage by nfc<PageProps> { props ->
    val (params) = useSearchParams()
    var message by useState("Completing Discord installation…")
    useEffectOnce {
        val code = params.get("code")
        val state = params.get("state")
        val guildId = params.get("guild_id")
        val flow = DiscordOAuthFlow(props.commander.tracingCannon(Uuid.random()))
        if (code == null || state == null || guildId == null || params.get("error") != null) {
            state?.let(flow::cancel)
            message = "Discord installation was cancelled or is incomplete. Please start again."
        } else {
            MainScope().launch {
                message = try {
                    if (flow.complete(code, guildId, state) == VoidResult.Accepted) {
                        "Discord integration installed."
                    } else {
                        "Discord installation could not be verified. Please start again from the original tab."
                    }
                } catch (_: Exception) {
                    "Unable to complete Discord installation. Please start again."
                }
            }
        }
    }
    InstallPageFrame {
        title = "Discord Install"
        +message
    }
}
