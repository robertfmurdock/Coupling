package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.client.components.MarkdownContent
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.slack.ReturnToCouplingButton
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import js.array.component1
import js.objects.unsafeJso
import react.Props
import react.PropsWithValue
import react.create
import react.dom.html.ReactHTML.div
import react.router.dom.useSearchParams

val SlackCallbackPage by nfc<PageProps> { props ->
    val (urlSearchParams) = useSearchParams()
    val code = urlSearchParams.get("code")
    val state = urlSearchParams.get("state")
    InstallPageFrame {
        title = "Slack Install"
        if (code == null || state == null) {
            +"code and state missing"
        } else {
            DataLoader(
                getDataAsync = {
                    props.commander.tracingCannon()
                        .fire(GrantSlackAccessCommand(code, state))
                },
                errorData = { VoidResult.Rejected },
                child = { SlackCallbackLoadContent.create { value = it } },
            )
        }
    }
}

val SlackCallbackLoadContent by nfc<PropsWithValue<DataLoadState<VoidResult>>> { props ->
    when (val data = props.value) {
        is EmptyState -> +"Empty"
        is PendingState -> +"Pending"
        is ResolvedState -> when (data.result) {
            VoidResult.Accepted -> SlackInstallSuccess {}
            VoidResult.Rejected -> +"Rejected"
            CommandResult.Unauthorized -> +"Unauthorized"
        }
    }
}

val SlackInstallSuccess by nfc<Props> {
    div { dangerouslySetInnerHTML = unsafeJso { __html = parse(MarkdownContent.content.installSuccessMd) } }
    ReturnToCouplingButton {
        to = "/parties"
    }
}
