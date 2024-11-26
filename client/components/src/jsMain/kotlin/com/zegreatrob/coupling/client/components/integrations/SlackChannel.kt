package com.zegreatrob.coupling.client.components.integrations

import react.ChildrenBuilder
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.html.InputType

fun ChildrenBuilder.slackChannel(slackChannel: String?) {
    label {
        this.htmlFor = "slack-channel-id"
        +"Slack Channel ID"
    }
    input {
        value = slackChannel
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
