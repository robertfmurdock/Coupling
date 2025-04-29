package com.zegreatrob.coupling.client.components.slack

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.integrations.slackChannel
import com.zegreatrob.coupling.client.components.integrations.slackTeam
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import react.router.dom.LinkProps
import react.useMemo
import react.useState
import kotlin.uuid.Uuid

external interface SlackConnectPageContentProps : Props {
    var parties: List<PartyDetails>
    var slackTeam: String
    var slackChannel: String
    var dispatchFunc: DispatchFunc<SaveSlackIntegrationCommand.Dispatcher>
}

@ReactFunc
val SlackConnectPageContent by nfc<SlackConnectPageContentProps> { props ->
    val partySelectId = useMemo { "${Uuid.random()}" }

    var command by useState {
        SaveSlackIntegrationCommand(
            partyId = props.parties.first().id,
            team = props.slackTeam,
            channel = props.slackChannel,
        )
    }
    var result by useState<VoidResult?>(null)
    val onSave = props.dispatchFunc { result = fire(command) }

    if (result == null) {
        ConfigForm(onSubmit = onSave) {
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
                                    key = party.id.value.toString()
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
        div { dangerouslySetInnerHTML = unsafeJso { __html = parse(loadMarkdownString("ConnectSuccess")) } }
        ReturnToCouplingButton { to = "/${command.partyId.value}/pairAssignments/current/" }
    }
}

val ReturnToCouplingButton by nfc<LinkProps> { props ->
    Link {
        draggable = false
        +props
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = orange
            +"Return to Coupling"
        }
    }
}
