package com.zegreatrob.coupling.client.components.integrations

import react.ChildrenBuilder
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.dom.ElementId
import web.html.InputType
import web.html.text

fun ChildrenBuilder.slackChannel(slackChannel: String?) {
    label {
        this.htmlFor = ElementId("slack-channel-id")
        +"Slack Channel ID"
    }
    input {
        value = slackChannel
        ariaLabel = "Slack Channel ID"
        this.name = "slackChannel"
        id = ElementId("slack-channel-id")
        this.type = InputType.text
        placeholder = ""
        this.list = ""
        this.checked = false
        this.onChange = { }
        autoFocus = false
        disabled = true
    }
}
