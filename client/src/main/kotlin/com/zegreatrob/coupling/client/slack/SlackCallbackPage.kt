package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.client.components.slack.ReturnToCouplingButton
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import react.Props
import react.router.dom.useSearchParams

val SlackCallbackPage by nfc<PageProps> { props ->
    val (urlSearchParams) = useSearchParams()
    val code = urlSearchParams["code"]
    val state = urlSearchParams["state"]
    SlackInstallPageFrame {
        if (code != null && state != null) {
            add(
                DataLoader(
                    getDataAsync = {
                        props.commander.tracingDispatcher().sdk.perform(
                            GrantSlackAccessCommand(code, state),
                        )
                    },
                    errorData = { VoidResult.Rejected },
                    children = {
                        when (it) {
                            is EmptyState -> +"Empty"
                            is PendingState -> +"Pending"
                            is ResolvedState -> when (it.result) {
                                VoidResult.Accepted -> SlackInstallSuccess {}
                                VoidResult.Rejected -> +"Rejected"
                                CommandResult.Unauthorized -> +"Unauthorized"
                            }
                        }
                    },
                ),
            )
        } else {
            +"code and state missing"
        }
    }
}

val SlackInstallSuccess by nfc<Props> {
    Markdown { +loadMarkdownString("InstallSuccess") }
    ReturnToCouplingButton {
        to = "/parties"
    }
}
