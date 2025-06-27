package com.zegreatrob.coupling.client.components.integrations

import react.ChildrenBuilder
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.dom.ElementId
import web.html.InputType
import web.html.text

fun ChildrenBuilder.slackTeam(slackTeam: String?) {
    label {
        this.htmlFor = ElementId("slack-team-id")
        +"Slack Team ID"
    }
    input {
        value = slackTeam
        ariaLabel = "Slack Team ID"
        this.name = "slackTeam"
        id = ElementId("slack-team-id")
        this.type = InputType.text
        disabled = true
        placeholder = ""
        this.list = ""
        this.checked = false
        this.onChange = { }
        autoFocus = false
    }
}
