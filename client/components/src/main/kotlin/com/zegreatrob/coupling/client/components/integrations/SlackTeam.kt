package com.zegreatrob.coupling.client.components.integrations

import react.ChildrenBuilder
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.html.InputType

fun ChildrenBuilder.slackTeam(slackTeam: String?) {
    label { this.htmlFor = "slack-team-id"; +"Slack Team ID" }
    input {
        value = slackTeam
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
