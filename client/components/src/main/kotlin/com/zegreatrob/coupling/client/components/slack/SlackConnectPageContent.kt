package com.zegreatrob.coupling.client.components.slack

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.useMemo
import react.useState
import web.html.InputType

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

    val onSave = props.dispatchFunc({ command }, { })

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
                li {
                    label { this.htmlFor = "slack-team-id"; +"Slack Team ID" }
                    input {
                        value = props.slackTeam
                        ariaLabel = "Slack Team ID"
                        this.name = "slackTeam"
                        id = "slack-team-id"
                        this.type = InputType.text
                        disabled = true
                        placeholder = ""
                        this.list = ""
                        this.checked = false
                        this.onChange = { }
                        autoFocus = false
                    }
                }
                li {
                    label { this.htmlFor = "slack-channel-id"; +"Slack Channel ID" }
                    input {
                        value = props.slackChannel
                        ariaLabel = "Slack Channel ID"
                        this.name = "slackChannel"
                        id = "slack-channel-id"
                        this.type = InputType.text
                        placeholder = ""
                        this.list = ""
                        this.checked = false
                        this.onChange = { }
                        autoFocus = false
                        disabled = true
                    }
                }
            }
        }
    }
}
