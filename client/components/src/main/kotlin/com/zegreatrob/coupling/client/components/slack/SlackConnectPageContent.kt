package com.zegreatrob.coupling.client.components.slack

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.components.integrations.slackChannel
import com.zegreatrob.coupling.client.components.integrations.slackTeam
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import react.useMemo
import react.useState

data class SlackConnectPageContent(
    val parties: List<PartyDetails>,
    val slackTeam: String,
    val slackChannel: String,
    val dispatchFunc: DispatchFunc<out SaveSlackIntegrationCommand.Dispatcher>,
) : DataPropsBind<SlackConnectPageContent>(
    slackConnectPageContent,
)

val slackConnectPageContent by ntmFC<SlackConnectPageContent> { props ->
    val partySelectId = useMemo { "${uuid4()}" }

    var command by useState {
        SaveSlackIntegrationCommand(
            partyId = props.parties.first().id,
            team = props.slackTeam,
            channel = props.slackChannel,
        )
    }
    var result by useState<VoidResult?>(null)
    val onSave = props.dispatchFunc({ command }, { result = it })

    if (result == null) {
        ConfigForm {
            onSubmit = onSave

            div {
                Editor {
                    li {
                        label {
                            htmlFor = partySelectId
                            +"Party"
                        }
                        select {
                            id = partySelectId
                            name = "party"
                            value = command.partyId.value
                            onChange = { command = command.copy(partyId = PartyId(it.target.value)) }
                            props.parties.map { party ->
                                val partyName = party.name
                                option {
                                    key = party.id.value
                                    value = party.id.value
                                    label = partyName
                                    if (partyName != null) {
                                        ariaLabel = partyName
                                    }
                                }
                            }
                        }
                        span {
                            +"Which party would you like to connect to a Slack channel?"
                        }
                    }
                    li { slackTeam(props.slackTeam) }
                    li { slackChannel(props.slackChannel) }
                }
            }
        }
    } else {
        Markdown { +loadMarkdownString("ConnectSuccess") }
        Link {
            to = "/${command.partyId.value}/pairAssignments/current/"
            draggable = false
            add(CouplingButton(large, orange)) {
                +"Return to Coupling"
            }
        }
    }
}
