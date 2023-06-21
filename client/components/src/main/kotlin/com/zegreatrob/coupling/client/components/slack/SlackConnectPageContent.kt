package com.zegreatrob.coupling.client.components.slack

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
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
                        onChange = {
                            println("change")
                            command = command.copy(partyId = PartyId(it.target.value))
                            println("command is now $command")
                        }
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
                    configInput(
                        labelText = "Slack Team ID",
                        id = "slack-team-id",
                        name = "slackTeam",
                        value = "",
                        type = InputType.text,
                        onChange = {},
                    )
                }
                li {
                    configInput(
                        labelText = "Slack Channel ID",
                        id = "slack-channel-id",
                        name = "slackChannel",
                        value = "",
                        type = InputType.text,
                        onChange = {},
                    )
                }
            }
        }
    }
}
