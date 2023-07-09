package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.integrations.slackChannel
import com.zegreatrob.coupling.client.components.integrations.slackTeam
import com.zegreatrob.coupling.client.incubating.AddToSlackButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import web.cssom.Display
import web.cssom.TextAlign
import web.cssom.number

data class IntegrationContent(
    val party: PartyDetails,
    val integration: PartyIntegration?,
    val addToSlackUrl: String,
) : DataPropsBind<IntegrationContent>(integrationContent)

val integrationContent by ntmFC<IntegrationContent> { props ->
    val (party, integrations) = props
    ConfigFrame {
        div {
            ConfigHeader {
                this.party = party
                +"Integrations!"
            }
            h2 { +"Any integrations enabled for this party will show up here!" }

            if (integrations != null) {
                Editor {
                    li { slackTeam(integrations.slackTeam) }
                    li { slackChannel(integrations.slackChannel) }
                }
                integrations.slackTeam
            } else {
                +"Looks like this party doesn't have any integrations currently."
            }

            h2 { +"Want to add an integration?" }

            div {
                css { display = Display.flex }
                div {
                    css { flexGrow = number(1.0) }
                    p { AddToSlackButton { url = props.addToSlackUrl } }
                }
                div {
                    css {
                        textAlign = TextAlign.left
                        flexGrow = number(1.0)
                    }
                    p { +"For Slack, first you'll have to install the Coupling app to your Slack workspace." }
                    p { +"Then you'll need to connect a Slack channel to your Coupling Party." }
                    p { +"Type `/coupling` in Slack for the next steps." }
                }
            }
        }
    }
}
